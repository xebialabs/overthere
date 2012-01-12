/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
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

package com.xebialabs.overthere;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PATH_SHARE_MAPPINGS;
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

import com.google.common.collect.ImmutableMap;

public class OverthereOnWindowsItest extends ParametrizedOverthereConnectionItestBase {

	private static final String ADMINISTRATIVE_USER_ITEST_USERNAME = "Administrator";
	private static final String ADMINISTRATIVE_USER_ITEST_PASSWORD = "iW8tcaM0d";

	private static final String REGULAR_USER_ITEST_USERNAME = "overthere";
	private static final String REGULAR_USER_ITEST_PASSWORD = "wLitdMy@:;<>KY9";
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
	    return partialOptions;
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
		return partialOptions;
	}

	private static ConnectionOptions createSshSftpCygwinWithAdministrativeUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_CYGWIN);
		options.set(PORT, 22);
		options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		return options;
	}

	private static ConnectionOptions createSshSftpCygwinWithRegularUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_CYGWIN);
		options.set(PORT, 22);
		options.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
		options.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
		return options;
	}

	private static ConnectionOptions createSshSftpWinSshdWithAdministrativeUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_WINSSHD);
		options.set(PORT, 2222);
		options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		return options;
	}

	private static ConnectionOptions createSshSftpWinSshdWithRegularUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_WINSSHD);
		options.set(PORT, 2222);
		options.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
		options.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
		return options;
	}

}

