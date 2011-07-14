package com.xebialabs.overthere.winrm;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.cifs.CifsTelnetConnection;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static com.xebialabs.overthere.ConnectionOptions.PORT;

public class CifsWinRMConnection extends CifsTelnetConnection {

	private final WinRMClient winRMClient;

	private final int port;

	public CifsWinRMConnection(String type, ConnectionOptions options, WinRMClient winRMClient) {
		super(type, options);
		this.winRMClient = winRMClient;
		port = options.get(PORT, 567);
	}

	@Override
	public void doDisconnect() {
		System.out.println("CifsWinRMConnection.doDisconnect.....");
	}

	@Override
	public OverthereProcess startProcess(CmdLine commandLine) {
		final String commandLineForExecution = commandLine.toCommandLine(getHostOperatingSystem(), false);
		final String commandLineForLogging = commandLine.toCommandLine(getHostOperatingSystem(), true);
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
					throw new RuntimeIOException("Cannot execute command " + commandLineForLogging + " at " + winRMClient.getTargetURL(), exc);
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
		return "cifs_winrm://" + username + "@" + address + ":" + port;
	}


}
