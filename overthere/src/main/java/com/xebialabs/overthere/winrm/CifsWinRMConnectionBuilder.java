package com.xebialabs.overthere.winrm;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;

import java.net.MalformedURLException;
import java.net.URL;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.overthere.winrm.tokengenerator.BasicTokenGenerator;

@com.xebialabs.overthere.spi.Protocol(name = "cifs_winrm")
public class CifsWinRMConnectionBuilder implements OverthereConnectionBuilder {

	public static final String PROTOCOL = "protocol";
	public static final String CONTEXT = "context";
	public static final String TIMEMOUT = "timemout";
	public static final String ENVELOP_SIZE = "envelopSize";
	public static final String LOCALE = "locale";
	public static final String AUTHENTICATION = "authenticationMode";

	//eg PT60.000S I don't know what is this format ...
	public static final String DEFAULT_TIMEOUT = "PT60.000S";
	public static final long DEFAULT_MAX_ENV_SIZE = 153600;
	public static final String DEFAULT_LOCALE = "en-US";
	public static final String DEFAULT_WINRM_CONTEXT = "/wsman";
	public static final int DEFAULT_HTTP_PORT = 5985;
	public static final int DEFAULT_HTTPS_PORT = 5986;


	private final HttpConnector httpConnector;
	private final URL targetURL;
	private CifsWinRMConnection connection;
	private final WinRMClient winRMClient;

	public CifsWinRMConnectionBuilder(String type, ConnectionOptions options) {
		Protocol protocol = options.get(PROTOCOL, Protocol.HTTP);
		TokenGenerator tokenGenerator = getTokenGenerator(options);
		targetURL = getTargetURL(protocol, options);
		httpConnector = HttpConnectorFactory.newHttpConnector(protocol, targetURL, tokenGenerator);

		winRMClient = new WinRMClient(httpConnector, targetURL);
		winRMClient.setTimeout(options.<String>get(TIMEMOUT, DEFAULT_TIMEOUT));
		winRMClient.setEnvelopSize(options.<Long>get(ENVELOP_SIZE, DEFAULT_MAX_ENV_SIZE));
		winRMClient.setLocale(options.get(LOCALE, DEFAULT_LOCALE));

		connection = new CifsWinRMConnection(type, options, winRMClient);
	}

	private URL getTargetURL(Protocol protocol, ConnectionOptions options) {
		String address = options.get(ADDRESS);
		String context = options.get(CONTEXT, DEFAULT_WINRM_CONTEXT);
		int port = options.get(PORT, DEFAULT_HTTP_PORT);

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
		AuthenticationMode authenticationMode = options.get(AUTHENTICATION);

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
