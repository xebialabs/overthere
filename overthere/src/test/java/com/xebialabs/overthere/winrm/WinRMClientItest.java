/*
 * This file is part of WinRM.
 *
 * WinRM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WinRM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WinRM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.winrm;


import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;

public class WinRMClientItest  {


	protected String type;

	protected ConnectionOptions options;

	protected OverthereConnection connection;

	static final String DEFAULT_SERVER = "WIN-2MGY3RY6XSH.deployit.local";
	static final int DEFAULT_PORT = WinRMClient.DEFAULT_HTTP_PORT;
	static final String DEFAULT_USERNAME = "hilversum";
	static final String DEFAULT_PASSWORD = "Xe%%bia";
	static final String KRB5_CONF = "src/test/resources/krb5.conf";
	static final String LOGIN_CONF = "src/test/resources/login.conf";

	@Before
	public void setup() {
		System.setProperty("java.security.krb5.conf", KRB5_CONF);
		System.setProperty("java.security.auth.login.config", LOGIN_CONF);
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

	}

	@After
	public void tearDown() {
		System.setProperty("java.security.krb5.conf", "");
		System.setProperty("java.security.auth.login.config", "");
		System.setProperty("javax.security.auth.useSubjectCredsOnly", "");
	}

	@Test(expected = RuntimeException.class)
	public void testWinRMClientMisConfigurationBlankForLogin() {
		tearDown();
		connection.execute(capturingHandler(), CmdLine.build("ipconfig"));
	}

	@Test(expected = RuntimeException.class)
	public void testWinRMClientMisConfigurationWrongPathForLogin() {
		tearDown();
		System.setProperty("java.security.auth.login.config", "/not/exist/file.conf");
		connection.execute(capturingHandler(), CmdLine.build("ipconfig"));
	}

	@Test(expected = RuntimeException.class)
	public void testWinRMClientMisConfigurationBlankForKerberos() {
		tearDown();
		System.setProperty("java.security.auth.login.config", LOGIN_CONF);
		System.setProperty("java.security.krb5.conf", "");
		connection.execute(capturingHandler(), CmdLine.build("ipconfig"));
	}

	@Test(expected = RuntimeException.class)
	public void testWinRMClientMisConfigurationWrongPathForKerberos() {
		tearDown();
		System.setProperty("java.security.auth.login.config", LOGIN_CONF);
		System.setProperty("java.security.krb5.conf", "/path/to/nowhere/file.conf");
		connection.execute(capturingHandler(), CmdLine.build("ipconfig"));
	}

	@Before
	public void setTypeAndOptions() throws Exception {
		type = "cifs_winrm";
		options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(ADDRESS, DEFAULT_SERVER);
		options.set(USERNAME, DEFAULT_USERNAME);
		// ensure the test user contains some reserved characters such as ';', ':' or '@'
		options.set(PASSWORD, DEFAULT_PASSWORD);
		options.set(PORT, DEFAULT_PORT);
		options.set("CONTEXT", WinRMClient.DEFAULT_WINRM_CONTEXT);
		options.set("PROTOCOL", Protocol.HTTP);
		options.set("AUTHENTICATION", AuthenticationMode.KERBEROS);
		connection = Overthere.getConnection(type, options);

	}

	@After
	public void disconnect() {
		if (connection != null) {
			try {
				connection.disconnect();
				connection = null;
			} catch (Exception exc) {
				System.out.println("Exception while disconnecting at end of test case:");
				exc.printStackTrace(System.out);
			}
		}
	}
}
