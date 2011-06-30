package com.xebialabs.overthere.winrm;

import static com.xebialabs.overthere.ConnectionOptions.*;

public class WinRMHttpBasicItest extends WinRMItestBase {

	@Override
	protected void setTypeAndOptions() throws Exception {
		super.setTypeAndOptions();
		options.set(USERNAME, DEFAULT_USERNAME);
		options.set(PASSWORD, DEFAULT_PASSWORD);
		options.set(PORT, DEFAULT_PORT);
		options.set("PROTOCOL", Protocol.HTTP);
		options.set("AUTHENTICATION", AuthenticationMode.BASIC);
	}
}
