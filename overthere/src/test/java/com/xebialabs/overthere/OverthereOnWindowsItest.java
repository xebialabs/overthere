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
import static com.xebialabs.overthere.ConnectionOptions.*;
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
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PATH_SHARE_MAPPINGS;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTP;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTPS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_PTY;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.LOCAL_PORT_FORWARDS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP_CYGWIN;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP_WINSSHD;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionType;
import jcifs.util.LogStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableMap;

public class OverthereOnWindowsItest extends ParametrizedOverthereConnectionItestBase {

	private static final String ADMINISTRATIVE_USER_ITEST_USERNAME = "Administrator";
	private static final String ADMINISTRATIVE_USER_ITEST_PASSWORD = "iW8tcaM0d";

	private static final String REGULAR_USER_ITEST_USERNAME = "overthere";
	private static final String REGULAR_USER_ITEST_PASSWORD = "wLitdMy@:;<>KY9";
	private static OverthereConnection connection;
	// The password for the regular user includes special characters to test that they get encoded correctly

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
		lopco.add(new Object[] { CIFS_PROTOCOL, createCifsTelnetWithAdministrativeUserOptions(), "com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection" });
		lopco.add(new Object[] { CIFS_PROTOCOL, createCifsTelnetWithRegularUserOptions(), "com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection" });
		lopco.add(new Object[] { CIFS_PROTOCOL, createCifsWinRmHttpWithAdministrativeUserOptions(), "com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection" });
		lopco.add(new Object[] { CIFS_PROTOCOL, createCifsWinRmHttpsWithAdministrativeUserOptions(), "com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection" });
		lopco.add(new Object[] { SSH_PROTOCOL, createSshSftpCygwinWithAdministrativeUserOptions(), "com.xebialabs.overthere.ssh.SshSftpCygwinConnection" });
		lopco.add(new Object[] { SSH_PROTOCOL, createSshSftpCygwinWithRegularUserOptions(), "com.xebialabs.overthere.ssh.SshSftpCygwinConnection" });
		lopco.add(new Object[] { SSH_PROTOCOL, createSshSftpWinSshdWithAdministrativeUserOptions(), "com.xebialabs.overthere.ssh.SshSftpWinSshdConnection" });
		lopco.add(new Object[] { SSH_PROTOCOL, createSshSftpWinSshdWithRegularUserOptions(), "com.xebialabs.overthere.ssh.SshSftpWinSshdConnection" });
		return lopco;
	}

	private static ConnectionOptions createCifsTelnetWithAdministrativeUserOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, TELNET);
		partialOptions.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		partialOptions.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		partialOptions.set(PORT, DEFAULT_TELNET_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
		partialOptions.set(TUNNEL, createPartialTunnelOptions());
	    return partialOptions;
    }

	private static ConnectionOptions createPartialTunnelOptions() {
		ConnectionOptions tunnelOptions = new ConnectionOptions();
		tunnelOptions.set(OPERATING_SYSTEM, WINDOWS);
		tunnelOptions.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.TUNNEL);
		tunnelOptions.set(PORT, 2222);
		tunnelOptions.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		tunnelOptions.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		tunnelOptions.set(LOCAL_PORT_FORWARDS, "2022:TARGET:22,2023:TARGET:23,2445:TARGET:445,22222:TARGET:2222,25985:TARGET:5985,25986:TARGET:5986");
		return tunnelOptions;
	}

	private static ConnectionOptions createCifsTelnetWithRegularUserOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, TELNET);
		partialOptions.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
		partialOptions.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
		partialOptions.set(PORT, DEFAULT_TELNET_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
		partialOptions.set(TEMPORARY_DIRECTORY_PATH, "C:\\overthere\\tmp");
		partialOptions.set(PATH_SHARE_MAPPINGS, ImmutableMap.of("C:\\overthere", "sharethere"));
		partialOptions.set(TUNNEL, createPartialTunnelOptions());
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpWithAdministrativeUserOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, WINRM_HTTP);
		partialOptions.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		partialOptions.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PORT, DEFAULT_WINRM_HTTP_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
		partialOptions.set(TUNNEL, createPartialTunnelOptions());
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpsWithAdministrativeUserOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, WINRM_HTTPS);
		partialOptions.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		partialOptions.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PORT, DEFAULT_WINRM_HTTPS_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
		partialOptions.set(TUNNEL, createPartialTunnelOptions());
		return partialOptions;
	}

	private static ConnectionOptions createSshSftpCygwinWithAdministrativeUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_CYGWIN);
		options.set(PORT, 22);
		options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		options.set(TUNNEL, createPartialTunnelOptions());
		return options;
	}

	private static ConnectionOptions createSshSftpCygwinWithRegularUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_CYGWIN);
		options.set(PORT, 22);
		options.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
		options.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
		options.set(TUNNEL, createPartialTunnelOptions());
		return options;
	}

	private static ConnectionOptions createSshSftpWinSshdWithAdministrativeUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_WINSSHD);
		options.set(PORT, 2222);
		options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		options.set(TUNNEL, createPartialTunnelOptions());
		return options;
	}

	private static ConnectionOptions createSshSftpWinSshdWithRegularUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_WINSSHD);
		options.set(PORT, 2222);
		options.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
		options.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
		options.set(ALLOCATE_PTY, "xterm:80:24:0:0");
		options.set(TUNNEL, createPartialTunnelOptions());
		return options;
	}

}
