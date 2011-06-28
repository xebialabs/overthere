package com.xebialabs.overthere.winrm;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.CifsTelnetConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.overthere.winrm.tokengenerator.KerberosTokenGenerator;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static com.xebialabs.overthere.ConnectionOptions.*;

@com.xebialabs.overthere.spi.Protocol(name = "cifs_winrm")
public class CifsWinRMConnection extends CifsTelnetConnection implements OverthereConnectionBuilder {

	private final String address;
	private final int port;
	private final String username;
	private final String password;
	private final Protocol protocol;
	private final AuthenticationMode authenticationMode;
	private final String context;


	private HttpConnector httpConnector;
	private URL targetURL;

	public CifsWinRMConnection(String type, ConnectionOptions options) {
		super(type, options);
		this.address = options.get(ADDRESS);
		this.username = options.get(USERNAME);
		this.password = options.get(PASSWORD);
		this.port = options.get(PORT, 567);
		this.context = options.get("CONTEXT");
		this.authenticationMode = options.get("AUTHENTICATION");
		protocol = options.get("PROTOCOL", Protocol.HTTP);

		targetURL = getTargetURL();
		httpConnector = HttpConnectorFactory.newHttpConnector(protocol, targetURL, getTokenGenerator());

	}

	private URL getTargetURL() {
		try {
			return new URL(protocol.get(), address, port, context);
		} catch (MalformedURLException e) {
			throw new WinRMRuntimeIOException("Cannot build a new URL using host " + this, e);
		}
	}

	public TokenGenerator getTokenGenerator() {
		switch (this.authenticationMode) {
			case KERBEROS:
				return new KerberosTokenGenerator(address, username, password);
		}
		throw new IllegalArgumentException("the " + this.authenticationMode + " is not supported");

	}

	@Override
	public void doDisconnect() {

	}

	@Override
	public OverthereProcess startProcess(CmdLine commandLine) {
		final String commandLineForExecution = commandLine.toCommandLine(getHostOperatingSystem(), false);
		final String commandLineForLogging = commandLine.toCommandLine(getHostOperatingSystem(), true);
		final WinRMClient winRMClient = new WinRMClient(httpConnector, targetURL);

		return new OverthereProcess() {

			@Override
			public OutputStream getStdin() {
				return new ByteArrayOutputStream();
			}

			@Override
			public InputStream getStdout() {
				return winRMClient.getStdoutStream();
			}

			@Override
			public InputStream getStderr() {
				return winRMClient.getStderrStream();
			}

			@Override
			public int waitFor() throws InterruptedException {
				try {
					winRMClient.runCmd(commandLineForExecution);
					return winRMClient.getExitCode();
				} catch (RuntimeException exc) {
					throw new RuntimeIOException("Cannot execute command " + commandLineForLogging + " at " + targetURL, exc);
				}
			}

			@Override
			public void destroy() {
				winRMClient.destroy();
			}
		};
	}

	@Override
	public String toString() {
		return "cifs_winrm://" + username + "@" + address+":"+port;
	}

	@Override
	public OverthereConnection connect() {
		return this;
	}

}
