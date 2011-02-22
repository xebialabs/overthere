package com.xebialabs.overthere.local;

import static java.io.File.createTempFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.common.AbstractHostConnection;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.common.ErrorStreamToCallbackHandler;
import com.xebialabs.overthere.common.InputResponseHandler;
import com.xebialabs.overthere.common.OutputStreamToCallbackHandler;

/**
 * A connection to the local host.
 */
@Protocol(name = "local")
public class LocalHostConnection extends AbstractHostConnection implements HostConnectionBuilder, HostConnection {

	/**
	 * Constructs a connection to the local host.
	 */
	public LocalHostConnection(String type, ConnectionOptions options) {
		super(type, determineOs(), options);
	}

	private static OperatingSystemFamily determineOs() {
		return SystemUtils.IS_OS_WINDOWS ? OperatingSystemFamily.WINDOWS : OperatingSystemFamily.UNIX;
	}

	public HostConnection connect() {
		return this;
	}

	public HostFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
		try {
			File tempFile = createTempFile(prefix, suffix, new File(getTemporaryDirectory().getPath()));
			// FIXME: Need to delete this file to make test work, but isn't it better to NOT do that so that a tempfile with the same name is not accidentally
			// created simultaneously?
			tempFile.delete();
			return new LocalHostFile(this, tempFile);
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public HostFile getFile(String hostPath) throws RuntimeIOException {
		return new LocalHostFile(this, new File(hostPath));
	}

	public HostFile getFile(HostFile parent, String child) throws RuntimeIOException {
		if (!(parent instanceof LocalHostFile)) {
			throw new IllegalStateException("parent is not a file on the local host");
		}
		File parentFile = ((LocalHostFile) parent).getFile();
		return new LocalHostFile(this, new File(parentFile, child));
	}

	@SuppressWarnings("unchecked")
	public int execute(CommandExecutionCallbackHandler handler, String... cmdarray) throws RuntimeIOException {
		return execute(handler, Collections.EMPTY_MAP, cmdarray);
	}

	public int execute(CommandExecutionCallbackHandler handler, Map<String, String> inputResponse, String... cmdarray) {
		String commandLineForLogging = encodeCommandLineForLogging(cmdarray);

		Process proc;
		try {
			if (getHostOperatingSystem() == OperatingSystemFamily.WINDOWS) {
				logger.debug("Enabling Windows specific command line encoding");
				logger.info("Executing local command: " + commandLineForLogging);
				proc = Runtime.getRuntime().exec(encodeCommandLineForExecution(cmdarray));
			} else {
				logger.info("Executing local command: " + commandLineForLogging);
				proc = Runtime.getRuntime().exec(cmdarray);
			}
		} catch (IOException exc) {
			throw new RuntimeIOException("Could not start local command: " + commandLineForLogging, exc);
		}

		InputResponseHandler responseHandler = new InputResponseHandler(handler, proc.getOutputStream(), inputResponse);
		Thread outputGobblerThread = new Thread(new OutputStreamToCallbackHandler(proc.getInputStream(), responseHandler));
		outputGobblerThread.start();

		Thread errorGobblerThread = new Thread(new ErrorStreamToCallbackHandler(proc.getErrorStream(), responseHandler));
		errorGobblerThread.start();

		try {
			outputGobblerThread.join();
			errorGobblerThread.join();
			return proc.waitFor();
		} catch (InterruptedException exc) {
			Thread.currentThread().interrupt();
			throw new AbortedException("Local command was interrupted: " + commandLineForLogging, exc);
		}
	}

	public CommandExecution startExecute(String... commandLine) {
		return null;
	}

	public String toString() {
		return "localhost";
	}

	private static Logger logger = LoggerFactory.getLogger(LocalHostConnection.class);

}
