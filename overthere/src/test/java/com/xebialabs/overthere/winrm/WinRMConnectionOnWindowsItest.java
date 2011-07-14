package com.xebialabs.overthere.winrm;

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
import static com.xebialabs.overthere.winrm.AuthenticationMode.BASIC;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.AUTHENTICATION;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.CONTEXT;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.DEFAULT_HTTPS_PORT;
import static com.xebialabs.overthere.winrm.CifsWinRMConnectionBuilder.DEFAULT_HTTP_PORT;
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
import com.xebialabs.overthere.ConnectionOptions;

@RunWith(Parameterized.class)
public class WinRMConnectionOnWindowsItest extends WinRMItestBase {

	private static final String DEFAULT_USERNAME = "overthere";
	private static final String DEFAULT_PASSWORD = "Y6VLCyXi62";


	protected static ItestHost host;

	private final ConnectionOptions partialOptions;

	@BeforeClass
	public static void setupHost() {
		host = getItestHost("overthere-win");
		host.setup();
	}
	
	@AfterClass
	public static void teardownHost() {
		if(host != null) {
			host.teardown();
		}
	}

	public WinRMConnectionOnWindowsItest(ConnectionOptions partialOptions) {
		this.partialOptions = partialOptions;
	}

	@Override
	protected void setTypeAndOptions() throws Exception {
		type = "cifs_winrm";
		options = new ConnectionOptions(partialOptions);
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(ADDRESS, host.getHostName());
		options.set(PORT, host.getPort((Integer) partialOptions.get(PORT)));
		options.set(CIFS_PORT, host.getPort(CIFS_PORT_DEFAULT));
	}

	@Parameters
	public static Collection<Object[]> createListOfPartialConnectionOptions() throws IOException {
		List<Object[]> lopco = newArrayList();
		lopco.add(new Object[] { createHttpBasicOptions() });
		lopco.add(new Object[] { createHttpsLazyBasicOptions() });
		return lopco;
	}

	private static ConnectionOptions createHttpBasicOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(USERNAME, DEFAULT_USERNAME);
		partialOptions.set(PASSWORD, DEFAULT_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PROTOCOL, HTTP);
		partialOptions.set(PORT, DEFAULT_HTTP_PORT);
		partialOptions.set(AUTHENTICATION, BASIC);
	    return partialOptions;
    }

	private static ConnectionOptions createHttpsLazyBasicOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(USERNAME, DEFAULT_USERNAME);
		partialOptions.set(PASSWORD, DEFAULT_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PROTOCOL, HTTPS);
		partialOptions.set(PORT, DEFAULT_HTTPS_PORT);
		partialOptions.set(AUTHENTICATION, BASIC);
		return partialOptions;
	}


}
