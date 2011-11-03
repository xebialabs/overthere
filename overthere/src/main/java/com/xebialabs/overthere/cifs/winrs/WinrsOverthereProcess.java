package com.xebialabs.overthere.cifs.winrs;

import com.google.common.base.Joiner;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class WinrsOverthereProcess implements OverthereProcess {

	private final ProcessBuilder processBuilder;
	private final Process process;

	public WinrsOverthereProcess(List<String> fullCommandLine) {
		processBuilder = new ProcessBuilder(fullCommandLine);
		try {
			process = processBuilder.start();
			process.getOutputStream().close(); // this is necessary to close the outputStream for winrs
			   								   // otherwise Winrs doesn't end the process
                                               // => Closing this stream soo early forced me to disable getStdin() below
		} catch (IOException e) {
			throw new RuntimeIOException("Winrs command start fails to start {}" + Joiner.on(" ").join(fullCommandLine), e);
		}
	}

	public OutputStream getStdin() {
		throw new UnsupportedOperationException("This protocol doesn't support stdin");
	}

	public InputStream getStdout() {
		return process.getInputStream();
	}

	public InputStream getStderr() {
		return process.getErrorStream();
	}

	public int waitFor() throws InterruptedException {
		return process.waitFor();
	}

	public void destroy() {
		process.destroy();
	}
}
