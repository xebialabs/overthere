package com.xebialabs.overthere.winrm;

import org.junit.After;
import org.junit.Before;

import static com.xebialabs.overthere.ConnectionOptions.*;

/**
 * sudo keytool -import -keystore src/test/resources/key/cacerts -alias WIN-2MGY3RY6XSH -file src/test/resources/key/remote.host.pem
 */
public class WinRMHttpsKerberosItest extends WinRMItestBase {

	@Override
	protected void setTypeAndOptions() throws Exception {
		super.setTypeAndOptions();
		options.set(USERNAME, DEFAULT_USERNAME);
		options.set(PASSWORD, DEFAULT_PASSWORD);
		options.set(PORT, CifsWinRMConnectionBuilder.DEFAULT_HTTPS_PORT);
		options.set(CifsWinRMConnectionBuilder.PROTOCOL, Protocol.HTTPS);
		options.set(CifsWinRMConnectionBuilder.AUTHENTICATION, AuthenticationMode.KERBEROS);
	}

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


}
