package com.xebialabs.overthere.winrm;

import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereConnectionItestBase;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

public abstract class WinRMItestBase extends OverthereConnectionItestBase {

	static final String KRB5_CONF = "src/test/resources/krb5.conf";
	static final String LOGIN_CONF = "src/test/resources/login.conf";

	@Test
	public void testWinRMClient() {
		CapturingOverthereProcessOutputHandler handler = capturingHandler();
		int res = connection.execute(handler, CmdLine.build("ipconfig"));
		assertThat(res, equalTo(0));
		assertThat(handler.getOutput(), containsString("Windows IP Configuration"));
	}

	@Test
	public void testWinRMClientWithoutPassword() {
		options.set(PASSWORD, "");
		CapturingOverthereProcessOutputHandler handler = capturingHandler();
		int res = connection.execute(handler, CmdLine.build("ipconfig"));
		assertThat(res, equalTo(0));
		assertThat(handler.getOutput(), containsString("Windows IP Configuration"));
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

}
