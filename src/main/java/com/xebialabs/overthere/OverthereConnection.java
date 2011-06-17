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
package com.xebialabs.overthere;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Closeables.closeQuietly;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.util.OverthereUtils.getBaseName;
import static com.xebialabs.overthere.util.OverthereUtils.getExtension;

import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection on a host (local or remote) on which to manipulate files and execute commands.
 */
public abstract class OverthereConnection {

	protected String type;

	protected OperatingSystemFamily os;

	protected String temporaryDirectoryPath;

	protected OverthereFile sessionTemporaryDirectory;

	protected boolean deleteTemporaryDirectoryOnDisconnect = true;

	/**
     * The timeout for opening a connection in milliseconds.
     */
    public static final int CONNECTION_TIMEOUT_MS = 120000;
	
	/**
	 * The number of tries made when creating a unique temporary file name.
	 */
	public static final long MAX_TEMP_RETRIES = 100;

	protected OverthereConnection(String type, OperatingSystemFamily os, ConnectionOptions options) {
		this.type = checkNotNull(type, "Cannot create HostConnection with null type");
		this.os = checkNotNull(os, "Cannot create HostConnection with null os");
		this.temporaryDirectoryPath = options.get(TEMPORARY_DIRECTORY_PATH, os.getDefaultTemporaryDirectoryPath());

		Object deleteTemporaryDirectoryOnDisconnectObject = options.get(ConnectionOptions.TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT);
		if(deleteTemporaryDirectoryOnDisconnectObject instanceof String && "false".equalsIgnoreCase((String) deleteTemporaryDirectoryOnDisconnectObject)) {
			deleteTemporaryDirectoryOnDisconnect = false;
		}
	}

	protected OverthereConnection(String type, ConnectionOptions options) {
		this(type, options.<OperatingSystemFamily> get(OPERATING_SYSTEM), options);
	}

	/**
	 * Return the OS family of the host.
	 * 
	 * @return the OS family
	 */
	public final OperatingSystemFamily getHostOperatingSystem() {
		return os;
	}

	/**
	 * Closes the host connection. Destroys any temporary files that may have been created on the host.
	 * 
	 * Never throws an exception, not even a {@link RuntimeException}
	 */
	public final void disconnect() {
		if (deleteTemporaryDirectoryOnDisconnect) {
			deleteTemporaryDirectory();
		}

        doDisconnect();

		logger.info("Disconnected from {}", this);
	}

    protected abstract void doDisconnect();

    protected synchronized OverthereFile getTempDirectory() throws RuntimeIOException {
		if (sessionTemporaryDirectory == null) {
			OverthereFile temporaryDirectory = getFile(temporaryDirectoryPath);
			Random r = new Random();
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
			String prefix = "deployit-" + dateFormat.format(new Date());
			String infix = "";
			String suffix = ".tmp";
			for (int i = 0; i < MAX_TEMP_RETRIES; i++) {
				OverthereFile tempDir = createSessionTempDirectory(temporaryDirectory, prefix + infix + suffix);
				if (tempDir != null) {
					sessionTemporaryDirectory = tempDir;
					logger.info("Created connection temporary directory " + sessionTemporaryDirectory);
					return sessionTemporaryDirectory;
				}
				infix = "-" + Long.toString(Math.abs(r.nextLong()));
			}
			throw new RuntimeIOException("Cannot create connection temporary directory on " + this);
		}
		return sessionTemporaryDirectory;
	}

	protected OverthereFile createSessionTempDirectory(OverthereFile systemTempDirectory, String name) {
		OverthereFile f = getFile(systemTempDirectory, name);
		if (!f.exists()) {
			f.mkdir();
			return f;
		}
		return null;
	}

	public void deleteTemporaryDirectory() {
		if (sessionTemporaryDirectory != null) {
			try {
				sessionTemporaryDirectory.deleteRecursively();
				logger.info("Removed connection temporary directory " + sessionTemporaryDirectory);
			} catch (RuntimeException exc) {
				logger.warn("Got exception while removing connection temporary directory " + sessionTemporaryDirectory, exc);
			}
		}
	}

	/**
	 * Creates a reference to a file on the host.
	 * 
	 * @param hostPath
	 *            the path of the host
	 * @return a reference to the file
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public abstract OverthereFile getFile(String hostPath);

	/**
	 * Creates a reference to a file in a directory on the host.
	 * 
	 * @param parent
	 *            the reference to the directory on the host
	 * @param child
	 *            the name of the file in the directory
	 * @return a reference to the file in the directory
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public abstract OverthereFile getFile(OverthereFile parent, String child);

	/**
	 * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically removed when this connection is closed.
	 * <b>N.B.:</b> The file is not actually created until a put method is invoked.
	 * 
	 * @param nameTemplate
	 *            the template on which to base the name of the temporary file. May be <code>null</code>.
	 * @return a reference to the temporary file on the host
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public final OverthereFile getTempFile(String nameTemplate) {
		String prefix, suffix;

		if (nameTemplate != null) {
			int pos = nameTemplate.lastIndexOf('/');
			if (pos != -1) {
				nameTemplate = nameTemplate.substring(pos + 1);
			}
			pos = nameTemplate.lastIndexOf('\\');
			if (pos != -1) {
				nameTemplate = nameTemplate.substring(pos + 1);
			}
		}

		if (isNullOrEmpty(nameTemplate)) {
			prefix = "hostsession";
			suffix = ".tmp";
		} else {
			prefix = getBaseName(nameTemplate);
			suffix = "." + getExtension(nameTemplate);
		}

		return getTempFile(prefix, suffix);
	}

	/**
	 * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically removed when this connection is closed.
	 * <b>N.B.:</b> The file is not actually created until a put method is invoked.
	 * 
	 * @param prefix
	 *            the prefix string to be used in generating the file's name; must be at least three characters long
	 * @param suffix
	 *            the suffix string to be used in generating the file's name; may be <code>null</code>, in which case the suffix ".tmp" will be used
	 * @return a reference to the temporary file on the host
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public abstract OverthereFile getTempFile(String prefix, String suffix);

	/**
	 * Executes a command with its arguments.
	 * 
	 * @param handler
	 *            the callback handler that will be invoked when the executed command generated output.
	 * @param commandLine
	 *            the command line to execute.
	 * @return the exit value of the executed command. Is 0 on succesfull execution.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public final int execute(final OverthereProcessOutputHandler handler, final CmdLine commandLine) {
		final OverthereProcess process = startProcess(commandLine);
		Thread stdoutReaderThread = null;
		Thread stderrReaderThread = null;
		try {
			stdoutReaderThread = new Thread("Stdout reader thread for " + this) {
				public void run() {
					InputStreamReader stdoutReader = new InputStreamReader(process.getStdout());
					try {
						int readInt = stdoutReader.read();
						StringBuffer lineBuffer = new StringBuffer();
						while (readInt > -1) {
							char c = (char) readInt;
							handler.handleOutput(c);
							if (c != '\r' && c != '\n') {
								lineBuffer.append(c);
							}
							if (c == '\n') {
								handler.handleOutputLine(lineBuffer.toString());
								lineBuffer = new StringBuffer();
							}
							readInt = stdoutReader.read();
						}
					} catch (Exception exc) {
						logger.error("An exception occured while reading from stdout", exc);
					} finally {
						closeQuietly(stdoutReader);
					}
				}
			};
			stdoutReaderThread.start();

			stderrReaderThread = new Thread("Stderr reader thread for " + this) {
				public void run() {
					InputStreamReader stderrReader = new InputStreamReader(process.getStderr());
					try {
						int readInt = stderrReader.read();
						StringBuffer lineBuffer = new StringBuffer();
						while (readInt > -1) {
							char c = (char) readInt;
							if (c != '\r' && c != '\n') {
								lineBuffer.append(c);
							}
							if (c == '\n') {
								handler.handleErrorLine(lineBuffer.toString());
								lineBuffer = new StringBuffer();
							}
							readInt = stderrReader.read();
						}
					} catch (Exception exc) {
						logger.error("An exception occured while reading from stderr", exc);
					} finally {
						closeQuietly(stderrReader);
					}
				}
			};
			stderrReaderThread.start();

			// FIXME: Add configurable wait-for timeout
			try {
				return process.waitFor();
			} catch (InterruptedException exc) {
				Thread.currentThread().interrupt();
				process.destroy();
				throw new RuntimeIOException("Execution interrupted", exc);
			}
		} finally {
			if (stdoutReaderThread != null) {
				try {
					// interrupt the stdout reader thread in case it is stuck waiting for output that will never come
					stdoutReaderThread.interrupt();
					stdoutReaderThread.join();
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
			}
			if (stderrReaderThread != null) {
				try {
					// interrupt the stdout reader thread in case it is stuck waiting for output that will never come
					stderrReaderThread.interrupt();
					stderrReaderThread.join();
				} catch (InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}
			}
		}

	}

	/**
	 * Starts the execution of a command and gives the caller full control over the execution.
	 * 
	 * @param commandLine
	 *            the command line to execute.
	 * @return an object representing the executing command or <tt>null</tt> if this is not supported by the host connection.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public abstract OverthereProcess startProcess(CmdLine commandLine);

	/**
	 * Subclasses MUST implement toString properly.
	 */
	@Override
	public abstract String toString();

	private static Logger logger = LoggerFactory.getLogger(OverthereConnection.class);

}
