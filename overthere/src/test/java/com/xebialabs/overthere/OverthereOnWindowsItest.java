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
import static com.xebialabs.itest.ItestHostFactory.getItestHost;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.xebialabs.itest.ItestHost;

@RunWith(Parameterized.class)
public class OverthereOnWindowsItest extends OverthereConnectionItestBase {

	private static final String ITEST_USERNAME = "overthere";
	private static final String ITEST_PASSWORD = "wLitdMy@:;<>KY9";

	protected static ItestHost host;

	@BeforeClass
	public static void setupHost() {
		host = getItestHost("overthere-windows");
		host.setup();
	}
	
	@AfterClass
	public static void teardownHost() {
		if(host != null) {
			host.teardown();
		}
	}

	@Parameters
	public static Collection<Object[]> createListOfPartialConnectionOptions() throws IOException {
		List<Object[]> lopco = newArrayList();
		lopco.add(new Object[] { "cifs_telnet", createCifsTelnetOptions() });
		lopco.add(new Object[] { "cifs_winrm", createCifsWinRmHttpBasicOptions() });
		lopco.add(new Object[] { "cifs_winrm", createCifsWinRmHttpsBasicOptions() });
		return lopco;
	}

	private static ConnectionOptions createCifsTelnetOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(USERNAME, ITEST_USERNAME);
		partialOptions.set(PASSWORD, ITEST_PASSWORD);
		partialOptions.set(PORT, TELNET_PORT_DEFAULT);
		partialOptions.set(AUTHENTICATION, BASIC);
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpBasicOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(USERNAME, ITEST_USERNAME);
		partialOptions.set(PASSWORD, ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PROTOCOL, HTTP);
		partialOptions.set(PORT, DEFAULT_PORT_HTTP);
		partialOptions.set(AUTHENTICATION, BASIC);
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpsBasicOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(USERNAME, ITEST_USERNAME);
		partialOptions.set(PASSWORD, ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PROTOCOL, HTTPS);
		partialOptions.set(PORT, DEFAULT_PORT_HTTPS);
		partialOptions.set(AUTHENTICATION, BASIC);
		return partialOptions;
	}

	private final ConnectionOptions partialOptions;

	public OverthereOnWindowsItest(String type, ConnectionOptions partialOptions) {
		this.type = type;
		this.partialOptions = partialOptions;
	}


	@Override
	protected void setTypeAndOptions() throws Exception {
		options = new ConnectionOptions(partialOptions);
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(ADDRESS, host.getHostName());
		options.set(PORT, host.getPort((Integer) partialOptions.get(PORT)));
		options.set(CIFS_PORT, host.getPort(CIFS_PORT_DEFAULT));
	}

}
