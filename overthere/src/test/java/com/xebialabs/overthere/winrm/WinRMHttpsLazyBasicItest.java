package com.xebialabs.overthere.winrm;

import static com.xebialabs.overthere.ConnectionOptions.*;

public class WinRMHttpsLazyBasicItest extends WinRMItestBase {

	@Override
	protected void setTypeAndOptions() throws Exception {
		super.setTypeAndOptions();
		options.set(USERNAME, DEFAULT_USERNAME);
		options.set(PASSWORD, DEFAULT_PASSWORD);
		options.set(PORT, CifsWinRMConnectionBuilder.DEFAULT_HTTPS_PORT);
		options.set(CifsWinRMConnectionBuilder.PROTOCOL, Protocol.HTTPS_LAZY);
		options.set(CifsWinRMConnectionBuilder.AUTHENTICATION, AuthenticationMode.BASIC);
	}
}
