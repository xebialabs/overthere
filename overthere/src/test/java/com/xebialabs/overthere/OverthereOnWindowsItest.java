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
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;
import static com.xebialabs.overthere.winrm.AuthenticationMode.BASIC;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.AUTHENTICATION;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.CONTEXT;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.DEFAULT_PORT_HTTP;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.DEFAULT_PORT_HTTPS;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.DEFAULT_WINRM_CONTEXT;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.PROTOCOL;
import static com.xebialabs.overthere.winrm.Protocol.HTTP;
import static com.xebialabs.overthere.winrm.Protocol.HTTPS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.itest.ItestHost;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

@RunWith(Parameterized.class)
public class OverthereOnWindowsItest extends OverthereConnectionItestBase {

	private static final String DEFAULT_USERNAME = "overthere";
	// FIXME: ensure the test user contains some reserved characters such as ';', ':' or '@'
	// previous password:  "hello@:;<>myfriend"
	private static final String DEFAULT_PASSWORD = "Y6VLCyXi62";

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
		partialOptions.set(USERNAME, DEFAULT_USERNAME);
		partialOptions.set(PASSWORD, DEFAULT_PASSWORD);
		partialOptions.set(PORT, TELNET_PORT_DEFAULT);
		partialOptions.set(AUTHENTICATION, BASIC);
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpBasicOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(USERNAME, DEFAULT_USERNAME);
		partialOptions.set(PASSWORD, DEFAULT_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PROTOCOL, HTTP);
		partialOptions.set(PORT, DEFAULT_PORT_HTTP);
		partialOptions.set(AUTHENTICATION, BASIC);
	    return partialOptions;
    }

	private static ConnectionOptions createCifsWinRmHttpsBasicOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(USERNAME, DEFAULT_USERNAME);
		partialOptions.set(PASSWORD, DEFAULT_PASSWORD);
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

	@Test
	public void shouldListFilesInCDrive() throws IOException {
		OverthereFile cDrive = connection.getFile("C:");
		OverthereFile autoexecBat = cDrive.getFile("Program Files");
		List<OverthereFile> filesInCDrive = cDrive.listFiles();

		assertThat(filesInCDrive.contains(autoexecBat), equalTo(true));
	}

	@Test
	public void shouldExecuteSimpleCommand() {
		CapturingOverthereProcessOutputHandler capturingHandler = capturingHandler();
		int res = connection.execute(multiHandler(loggingHandler(logger), capturingHandler), CmdLine.build("ipconfig"));
		assertThat(res, equalTo(0));
		assertThat(capturingHandler.getOutput(), containsString("Windows IP Configuration"));
	}
	
	@Test
	public void shouldExecuteCommandWithArgument() {
		CapturingOverthereProcessOutputHandler capturingHandler = capturingHandler();
		int res = connection.execute(multiHandler(loggingHandler(logger), capturingHandler), CmdLine.build("dir", "/s"));
		assertThat(res, equalTo(0));
		assertThat(capturingHandler.getOutput(), containsString("Total Files Listed"));
	}

	@Test
	public void shoudNotExecuteIncorrectCommand() {
		int res = connection.execute(loggingHandler(logger), CmdLine.build("this-command-does-not-exist"));
		assertThat(res, not(equalTo(0)));
	}

	private static Logger logger = LoggerFactory.getLogger(OverthereOnWindowsItest.class);
	
}
