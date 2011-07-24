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
package com.xebialabs.overthere.cifs;

import static com.xebialabs.itest.ItestHostFactory.getItestHost;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsTelnetConnection.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsTelnetConnection.CIFS_PORT_DEFAULT;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.itest.ItestHost;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnectionItestBase;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

public class CifsTelnetConnectionItest extends OverthereConnectionItestBase {

	private static final String DEFAULT_USERNAME = "overthere";
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

	@Override
	protected void setTypeAndOptions() {
		type = "cifs_telnet";
		options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(ADDRESS, host.getHostName());
		options.set(PORT, host.getPort(23));
		options.set(CIFS_PORT, host.getPort(CIFS_PORT_DEFAULT));
		options.set(USERNAME, DEFAULT_USERNAME);
		// ensure the test user contains some reserved characters such as ';', ':' or '@'
		// previous password:  "hello@:;<>myfriend"
		options.set(PASSWORD, DEFAULT_PASSWORD);
	}

	@Test
	public void shouldListFilesInCDrive() throws IOException {
		OverthereFile cDrive = connection.getFile("C:");
		OverthereFile autoexecBat = cDrive.getFile("Program Files");
		List<OverthereFile> filesInCDrive = cDrive.listFiles();

		assertThat(filesInCDrive.contains(autoexecBat), equalTo(true));
	}

	@Test
	public void shouldExecuteCommand() {
		CapturingOverthereProcessOutputHandler capturingHandler = capturingHandler();
		OverthereProcessOutputHandler handler = multiHandler(loggingHandler(logger), capturingHandler);
		int res = connection.execute(handler, CmdLine.build("ipconfig"));
		assertThat(res, equalTo(0));
		assertThat(capturingHandler.getOutput(), containsString("Windows IP Configuration"));
	}

	@Test
	public void shoudNotExecuteIncorrectCommand() {
		CapturingOverthereProcessOutputHandler capturingHandler = capturingHandler();
		OverthereProcessOutputHandler handler = multiHandler(loggingHandler(logger), capturingHandler);
		int res = connection.execute(handler, CmdLine.build("this-command-does-not-exist"));
		assertThat(res, not(equalTo(0)));
	}
	
	

	private static Logger logger = LoggerFactory.getLogger(CifsTelnetConnectionItest.class);
	
}
