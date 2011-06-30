package com.xebialabs.overthere.winrm;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnectionItestBase;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;
import org.junit.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public abstract class WinRMItestBase extends OverthereConnectionItestBase {

	static final String DEFAULT_SERVER = "WIN-2MGY3RY6XSH.deployit.local";
	static final int DEFAULT_PORT = WinRMHost.DEFAULT_HTTP_PORT;
	static final String DEFAULT_USERNAME = "hilversum";
	static final String DEFAULT_PASSWORD = "Xe%%bia";
	static final String KRB5_CONF = "src/test/resources/krb5.conf";
	static final String LOGIN_CONF = "src/test/resources/login.conf";

	@Test
	public void testWinRMClient() {
		CapturingOverthereProcessOutputHandler handler = capturingHandler();
		int res = connection.execute(handler, CmdLine.build("ipconfig"));
		assertThat(res, equalTo(0));
		assertThat(handler.getOutput(), containsString("172.16.74.129"));
	}

	@Test
	public void testWinRMClientWithoutPassword() {
		options.set(PASSWORD, "");
		CapturingOverthereProcessOutputHandler handler = capturingHandler();
		int res = connection.execute(handler, CmdLine.build("ipconfig"));
		assertThat(res, equalTo(0));
		assertThat(handler.getOutput(), containsString("172.16.74.129"));
	}

	@Test
	public void testWinRMClientWrongCommandLine() {
		CapturingOverthereProcessOutputHandler handler = capturingHandler();
		int res = connection.execute(handler, CmdLine.build("ifconfig"));
		assertThat(res, equalTo(1));
		assertThat(handler.getError(), containsString("'ifconfig' is not recognized as an internal or external command"));
	}

	@Test
	public void testWinRMClientVerboseDir() {
		CapturingOverthereProcessOutputHandler handler = capturingHandler();
		int res = connection.execute(handler, CmdLine.build("dir", "/s"));
		assertThat(res, equalTo(0));
		assertThat(handler.getOutput(), containsString("Total Files Listed"));
	}

	@Override
	protected void setTypeAndOptions() throws Exception {
		type = "cifs_winrm";
		options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(ADDRESS, DEFAULT_SERVER);
		options.set(USERNAME, DEFAULT_USERNAME);
		// ensure the test user contains some reserved characters such as ';', ':' or '@'
		options.set(PASSWORD, DEFAULT_PASSWORD);
		options.set(PORT, DEFAULT_PORT);
		options.set("CONTEXT", WinRMHost.DEFAULT_WINRM_CONTEXT);
		options.set("PROTOCOL", Protocol.HTTP);
		options.set("AUTHENTICATION", AuthenticationMode.BASIC);

	}
}
