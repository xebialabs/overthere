/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.local;

import static com.xebialabs.overthere.OperatingSystemFamily.getLocalHostOperatingSystemFamily;
import static java.io.File.createTempFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.AbortedException;
import com.xebialabs.overthere.CommandExecution;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.HostConnection;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AbstractHostConnection;
import com.xebialabs.overthere.spi.ErrorStreamToCallbackHandler;
import com.xebialabs.overthere.spi.HostConnectionBuilder;
import com.xebialabs.overthere.spi.InputResponseHandler;
import com.xebialabs.overthere.spi.OutputStreamToCallbackHandler;
import com.xebialabs.overthere.spi.Protocol;

/**
 * A connection to the local host.
 */
@Protocol(name = "local")
public class LocalHostConnection extends AbstractHostConnection implements HostConnectionBuilder, HostConnection {

	/**
	 * Constructs a connection to the local host.
	 */
	public LocalHostConnection(String type, ConnectionOptions options) {
		super(type, getLocalHostOperatingSystemFamily(), options);
	}

	public HostConnection connect() {
		return this;
	}

	public OverthereFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
		try {
			File tempFile = createTempFile(prefix, suffix, new File(getTemporaryDirectory().getPath()));
			// FIXME: Need to delete this file to make test work, but isn't it better to NOT do that so that a tempfile with the same name is not accidentally
			// created simultaneously?
			// FIXME: Answer from VP: we have to decide on the semantics. How do you create a temporary directory?
			tempFile.delete();
			return new LocalOverthereFile(this, tempFile.getPath());
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public OverthereFile getFile(String hostPath) throws RuntimeIOException {
		return new LocalOverthereFile(this, hostPath);
	}

	public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
		if (!(parent instanceof LocalOverthereFile)) {
			throw new IllegalStateException("parent is not a LocalOverthereFile");
		}

		File childFile = new File(parent, child);
		return new LocalOverthereFile(this, childFile.getPath());
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
