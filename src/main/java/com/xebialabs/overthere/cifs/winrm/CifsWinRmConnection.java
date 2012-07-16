/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.cifs.winrm;

import java.net.MalformedURLException;
import java.net.URL;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.cifs.winrm.connector.JdkHttpConnector;
import com.xebialabs.overthere.cifs.winrm.connector.Kb5HttpConnector;
import com.xebialabs.overthere.cifs.winrm.connector.LaxJdkHttpConnector;
import com.xebialabs.overthere.cifs.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.overthere.cifs.winrm.tokengenerator.BasicTokenGenerator;
import com.xebialabs.overthere.spi.AddressPortMapper;
import org.ietf.jgss.GSSManager;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTP;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTP_KB5;

/**
 * A connection to a Windows host using CIFS and WinRM.
 * 
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator access is needed.</li>
 * <li>Can only authenticate with basic authentication to WinRM</li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public class CifsWinRmConnection extends CifsConnection {

	private final WinRmClient winRmClient;

	/**
	 * Creates a {@link CifsWinRmConnection}. Don't invoke directly. Use {@link Overthere#getConnection(String, ConnectionOptions)} instead.
	 */
	public CifsWinRmConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
		super(type, options, mapper, false);
		checkArgument(os == WINDOWS, "Cannot start a " + CIFS_PROTOCOL + ":%s connection to a non-Windows operating system", cifsConnectionType.toString().toLowerCase());

		TokenGenerator tokenGenerator = newTokenGenerator(cifsConnectionType,options);
		URL targetURL = getTargetURL(options);
		HttpConnector httpConnector = newHttpConnector(cifsConnectionType, targetURL, tokenGenerator, options);

		winRmClient = new WinRmClient(httpConnector, targetURL);
		winRmClient.setTimeout(options.get(TIMEMOUT, DEFAULT_TIMEOUT));
		winRmClient.setEnvelopSize(options.get(ENVELOP_SIZE, DEFAULT_ENVELOP_SIZE));
		winRmClient.setLocale(options.get(LOCALE, DEFAULT_LOCALE));
	}

	private static TokenGenerator newTokenGenerator(CifsConnectionType ccType,ConnectionOptions options) {
		switch (ccType) {
			case WINRM_HTTP:
			case WINRM_HTTPS:
				return new BasicTokenGenerator(options.<String>get(USERNAME), options.<String>get(PASSWORD));
			case WINRM_HTTP_KB5:
			case WINRM_HTTPS_KB5:
				//auth is handled by the connector
				return null;
		}
		throw new IllegalArgumentException("Invalid CIFS connection type " + ccType);
	}

	private URL getTargetURL(ConnectionOptions options) {
		String scheme = cifsConnectionType == WINRM_HTTP || cifsConnectionType == WINRM_HTTP_KB5 ? "http" : "https";
		String context = options.get(CONTEXT, DEFAULT_WINRM_CONTEXT);
		try {
			return new URL(scheme, address, port, context);
		} catch (MalformedURLException e) {
			throw new WinRMRuntimeIOException("Cannot build a new URL for " + this, e);
		}
	}

	public static HttpConnector newHttpConnector(CifsConnectionType ccType, URL targetURL, TokenGenerator tokenGenerator,
												 ConnectionOptions options) {
		switch (ccType) {
			case WINRM_HTTP:
				return new JdkHttpConnector(targetURL, tokenGenerator);
			case WINRM_HTTPS:
				return new LaxJdkHttpConnector(targetURL, tokenGenerator);
			case WINRM_HTTPS_KB5:
			case WINRM_HTTP_KB5:
				return new Kb5HttpConnector(targetURL, options);
		}
		throw new IllegalArgumentException("Invalid CIFS connection type " + ccType);
	}

	@Override
	public int execute(final OverthereProcessOutputHandler handler, final CmdLine commandLine) {
		String cmd = commandLine.toCommandLine(getHostOperatingSystem(), false);
		if(workingDirectory != null) {
			cmd = "CD " + workingDirectory.getPath() + " & " + cmd;
		}
		winRmClient.runCmd(cmd, handler);
		return winRmClient.getExitCode();
	}

}

