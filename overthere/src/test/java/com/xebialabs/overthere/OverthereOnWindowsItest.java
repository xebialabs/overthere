/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_TELNET_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTPS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTP_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTP;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTPS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP_CYGWIN;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP_WINSSHD;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

public class OverthereOnWindowsItest extends ParametrizedOverthereConnectionItestBase {

	private static final String ITEST_USERNAME = "overthere";
	private static final String ITEST_PASSWORD = "wLitdMy@:;<>KY9";

	@BeforeClass
	public static void setupItestHost() {
		setupItestHost("overthere-windows");
	}

	public OverthereOnWindowsItest(String type, ConnectionOptions partialOptions, String expectedConnectionClassName) {
	    super(type, partialOptions, expectedConnectionClassName);
    }

	@Parameters
	public static Collection<Object[]> createListOfPartialConnectionOptions() throws IOException {
		List<Object[]> lopco = newArrayList();
		lopco.add(new Object[] { CIFS_PROTOCOL, createCifsTelnetOptions(), "com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection" });
		lopco.add(new Object[] { CIFS_PROTOCOL, createCifsWinRmHttpOptions(), "com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection" });
		lopco.add(new Object[] { CIFS_PROTOCOL, createCifsWinRmHttpsOptions(), "com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection" });
		lopco.add(new Object[] { SSH_PROTOCOL, createSshSftpCygwinOptions(), "com.xebialabs.overthere.ssh.SshSftpCygwinConnection" });
		lopco.add(new Object[] { SSH_PROTOCOL, createSshSftpWinSshdOptions(), "com.xebialabs.overthere.ssh.SshSftpWinSshdConnection" });
		return lopco;
	}

	private static ConnectionOptions createCifsTelnetOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, TELNET);
		partialOptions.set(USERNAME, ITEST_USERNAME);
		partialOptions.set(PASSWORD, ITEST_PASSWORD);
		partialOptions.set(PORT, DEFAULT_TELNET_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, WINRM_HTTP);
		partialOptions.set(USERNAME, ITEST_USERNAME);
		partialOptions.set(PASSWORD, ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PORT, DEFAULT_WINRM_HTTP_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpsOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, WINRM_HTTPS);
		partialOptions.set(USERNAME, ITEST_USERNAME);
		partialOptions.set(PASSWORD, ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PORT, DEFAULT_WINRM_HTTPS_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
		return partialOptions;
	}

	private static ConnectionOptions createSshSftpCygwinOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_CYGWIN);
		options.set(PORT, 22);
		options.set(USERNAME, ITEST_USERNAME);
		options.set(PASSWORD, ITEST_PASSWORD);
		return options;
	}

	private static ConnectionOptions createSshSftpWinSshdOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_WINSSHD);
		options.set(PORT, 2222);
		options.set(USERNAME, ITEST_USERNAME);
		options.set(PASSWORD, ITEST_PASSWORD);
		return options;
	}

}
