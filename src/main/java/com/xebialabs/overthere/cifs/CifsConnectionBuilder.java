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

package com.xebialabs.overthere.cifs;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection;
import com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;

/**
 * Builds CIFS connections.
 */
@Protocol(name = CIFS_PROTOCOL)
public class CifsConnectionBuilder implements OverthereConnectionBuilder {

	/**
	 * Name of the protocol handled by this connection builder, i.e. "cifs".
	 */
	public static final String CIFS_PROTOCOL = "cifs";
	
	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify the {@link CifsConnectionType CIFS connection type} to use.
	 */
	public static final String CONNECTION_TYPE = "connectionType";
	
	/**
	 * Default port (23) used when the {@link #CONNECTION_TYPE CIFS connection type} is {#link {@link CifsConnectionType#TELNET TELNET}.
	 */
	public static final int DEFAULT_TELNET_PORT = 23;

	/**
	 * Default port (5985) used when the {@link #CONNECTION_TYPE CIFS connection type} is {#link {@link CifsConnectionType#WINRM_HTTP WINRM_HTTP}.
	 */
	public static final int DEFAULT_WINRM_HTTP_PORT = 5985;

	/**
	 * Default port (5986) used when the {@link #CONNECTION_TYPE CIFS connection type} is {#link {@link CifsConnectionType#WINRM_HTTPS WINRM_HTTPS}.
	 */
	public static final int DEFAULT_WINRM_HTTPS_PORT = 5986;

	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify the CIFS port to connect to.
	 */
	public static final String CIFS_PORT = "cifsPort";

	/**
	 * Default value (445) for the {@link ConnectionOptions connection option} used to specify the CIFS port to connect to.
	 */
	public static final int DEFAULT_CIFS_PORT = 445;

	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify the path to share mappings to use for CIFS, specified as a
	 * <tt>Map&lt;String, String&gt;</tt>, e.g. "C:\IBM\WebSphere" -> "WebSphere". If a path is not explicitly mapped to a share the administrative
	 * share will be used..
	 */
	public static final String PATH_SHARE_MAPPINGS = "pathShareMappings";

	/**
	 * Default value (empty map) for the {@link ConnectionOptions connection option} used to specify the path to share mappings to use for CIFS.
	 */
	public static final Map<String, String> PATH_SHARE_MAPPINGS_DEFAULT = ImmutableMap.of();

	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify the context (URI) used by WinRM.
	 */	
	public static final String CONTEXT = "winrmContext";

	/**
	 * Default value (/wsman) of the {@link ConnectionOptions connection option} used to specify the context (URI) used by WinRM.
	 */	
	public static final String DEFAULT_WINRM_CONTEXT = "/wsman";

	public static final String TIMEMOUT = "winrmTimeout";
	public static final String DEFAULT_TIMEOUT = "PT60.000S";

	public static final String ENVELOP_SIZE = "winrmEnvelopSize";
	public static final int DEFAULT_ENVELOP_SIZE = 153600;

	public static final String LOCALE = "winrmLocale";
	public static final String DEFAULT_LOCALE = "en-US";


	/**
	 * Kerberos JAAS option to enable debug output.
	 */
	public static final String DEBUG_KERBEROS_AUTH = "winrmDebugKerberosAuth";

	/**
	 * Specify strategy to trust certificates: "all", "self-signed", or "default" or null for default.
	 */
	public static final String HTTPS_CERTIFICATE_TRUST_STRATEGY = "winrmHttpsCertificateTrustStrategy";
	/**
	 * Specify the strategy to verify hostnames: "all" (trust all), or "default" or null for default
	 */
	public static final String HTTPS_HOSTNAME_VERIFY_STRATEGY = "winrmHttpsHostnameVerifyStrategy";

	private OverthereConnection connection;

	public CifsConnectionBuilder(String type, ConnectionOptions options, AddressPortMapper mapper) {
		CifsConnectionType cifsConnectionType = options.get(CONNECTION_TYPE);

		switch(cifsConnectionType) {
		case TELNET:
			connection = new CifsTelnetConnection(type, options, mapper);
			break;
		case WINRM_HTTP:
		case WINRM_HTTPS:
		case WINRM_HTTP_KB5:
		case WINRM_HTTPS_KB5:
			connection = new CifsWinRmConnection(type, options, mapper);
			break;
		default:
			throw new IllegalArgumentException("Unknown CIFS connection type " + cifsConnectionType);
		}
	}

	@Override
	public OverthereConnection connect() {
		return connection;
	}

	@Override
	public String toString() {
		return connection.toString();
	}

}

