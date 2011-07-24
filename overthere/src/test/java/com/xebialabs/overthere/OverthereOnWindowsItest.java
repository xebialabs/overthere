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
import static com.xebialabs.overthere.cifs.CifsTelnetConnection.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsTelnetConnection.CIFS_PORT_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsTelnetConnection.TELNET_PORT_DEFAULT;
import static com.xebialabs.overthere.winrm.AuthenticationMode.BASIC;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.AUTHENTICATION;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.CONTEXT;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.DEFAULT_PORT_HTTP;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.DEFAULT_PORT_HTTPS;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.DEFAULT_WINRM_CONTEXT;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.PROTOCOL;
import static com.xebialabs.overthere.winrm.Protocol.HTTP;
import static com.xebialabs.overthere.winrm.Protocol.HTTPS;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;

import com.xebialabs.overthere.cifs.CifsTelnetConnection;
import com.xebialabs.overthere.winrm.CifsWinRMConnection;

public class OverthereOnWindowsItest extends ParametrizedOverthereConnectionItestBase {

	private static final String ITEST_USERNAME = "overthere";
	private static final String ITEST_PASSWORD = "wLitdMy@:;<>KY9";

	static {
		itestHostLabel = "overthere-windows";
	}

	public OverthereOnWindowsItest(String type, ConnectionOptions partialOptions, String expectedConnectionClassName) {
	    super(type, partialOptions, expectedConnectionClassName);
    }

	@Parameters
	public static Collection<Object[]> createListOfPartialConnectionOptions() throws IOException {
		List<Object[]> lopco = newArrayList();
		lopco.add(new Object[] { "cifs_telnet", createCifsTelnetOptions(), CifsTelnetConnection.class.getName() });
		lopco.add(new Object[] { "cifs_winrm", createCifsWinRmHttpOptions(), CifsWinRMConnection.class.getName() });
		lopco.add(new Object[] { "cifs_winrm", createCifsWinRmHttpsOptions(), CifsWinRMConnection.class.getName() });
		return lopco;
	}

	private static ConnectionOptions createCifsTelnetOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(USERNAME, ITEST_USERNAME);
		partialOptions.set(PASSWORD, ITEST_PASSWORD);
		partialOptions.set(PORT, TELNET_PORT_DEFAULT);
		partialOptions.set(CIFS_PORT, CIFS_PORT_DEFAULT);
		partialOptions.set(AUTHENTICATION, BASIC);
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(USERNAME, ITEST_USERNAME);
		partialOptions.set(PASSWORD, ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PROTOCOL, HTTP);
		partialOptions.set(PORT, DEFAULT_PORT_HTTP);
		partialOptions.set(CIFS_PORT, CIFS_PORT_DEFAULT);
		partialOptions.set(AUTHENTICATION, BASIC);
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpsOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(USERNAME, ITEST_USERNAME);
		partialOptions.set(PASSWORD, ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PROTOCOL, HTTPS);
		partialOptions.set(PORT, DEFAULT_PORT_HTTPS);
		partialOptions.set(CIFS_PORT, CIFS_PORT_DEFAULT);
		partialOptions.set(AUTHENTICATION, BASIC);
		return partialOptions;
	}

}
