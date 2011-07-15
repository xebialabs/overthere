package com.xebialabs.overthere.winrm;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.winrm.AuthenticationMode.BASIC;
import static com.xebialabs.overthere.winrm.Protocol.HTTP;

import java.net.MalformedURLException;
import java.net.URL;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.overthere.winrm.tokengenerator.BasicTokenGenerator;

@com.xebialabs.overthere.spi.Protocol(name = "cifs_winrm")
public class CifsWinRMConnectionBuilder implements OverthereConnectionBuilder {

	public static final int DEFAULT_PORT_HTTP = 5985;
	public static final int DEFAULT_PORT_HTTPS = 5986;

	public static final String PROTOCOL = "winrmProtocol";
	public static final Protocol DEFAULT_PROTOCOL = Protocol.HTTP;
	
	public static final String CONTEXT = "winrmContext";
	public static final String DEFAULT_WINRM_CONTEXT = "/wsman";
	
	public static final String AUTHENTICATION = "winrmAuthenticationMode";
	private static final AuthenticationMode DEFAULT_AUTHENTICATION = BASIC;

	public static final String TIMEMOUT = "winrmTimeout";
	public static final String DEFAULT_TIMEOUT = "PT60.000S";
	// FIXME: Figure out what format this is

	public static final String ENVELOP_SIZE = "winrmEnvelopSize";
	public static final int DEFAULT_ENVELOP_SIZE = 153600;

	public static final String LOCALE = "winrmLocale";
	public static final String DEFAULT_LOCALE = "en-US";


	private final HttpConnector httpConnector;
	private final URL targetURL;
	private CifsWinRMConnection connection;
	private final WinRMClient winRMClient;

	public CifsWinRMConnectionBuilder(String type, ConnectionOptions options) {
		Protocol protocol = options.get(PROTOCOL, DEFAULT_PROTOCOL);
		TokenGenerator tokenGenerator = getTokenGenerator(options);
		targetURL = getTargetURL(protocol, options);
		httpConnector = HttpConnectorFactory.newHttpConnector(protocol, targetURL, tokenGenerator);

		winRMClient = new WinRMClient(httpConnector, targetURL);
		winRMClient.setTimeout(options.get(TIMEMOUT, DEFAULT_TIMEOUT));
		winRMClient.setEnvelopSize(options.get(ENVELOP_SIZE, DEFAULT_ENVELOP_SIZE));
		winRMClient.setLocale(options.get(LOCALE, DEFAULT_LOCALE));

		connection = new CifsWinRMConnection(type, options, winRMClient);
	}

	private URL getTargetURL(Protocol protocol, ConnectionOptions options) {
		String address = options.get(ADDRESS);
		String context = options.get(CONTEXT, DEFAULT_WINRM_CONTEXT);
		int port = options.get(PORT, protocol == HTTP ? DEFAULT_PORT_HTTP : DEFAULT_PORT_HTTPS);

		try {
			return new URL(protocol.get(), address, port, context);
		} catch (MalformedURLException e) {
			throw new WinRMRuntimeIOException("Cannot build a new URL using host " + this, e);
		}
	}


	private TokenGenerator getTokenGenerator(ConnectionOptions options) {
		String address = options.get(ADDRESS);
		String username = options.get(USERNAME);
		String password = options.get(PASSWORD);
		AuthenticationMode authenticationMode = options.get(AUTHENTICATION, DEFAULT_AUTHENTICATION);

		switch (authenticationMode) {
			case BASIC:
				return new BasicTokenGenerator(address, username, password);
		}
		throw new IllegalArgumentException("the " + authenticationMode + " is not supported");
	}

	@Override
	public OverthereConnection connect() {
		return connection;
	}

	public String toString() {
		return connection.toString();
	}
}
