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
import static com.xebialabs.overthere.ConnectionOptions.CONNECTION_TIMEOUT_MILLIS;
import static com.xebialabs.overthere.ConnectionOptions.CONNECTION_TIMEOUT_MILLIS_DEFAULT;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.util.OverthereUtils.getBaseName;
import static com.xebialabs.overthere.util.OverthereUtils.getExtension;

import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection on a host (local or remote) on which to manipulate files and execute commands.
 * 
 * All methods in this interface may throw a {@link RuntimeIOException} if an error occurs. Checked {@link IOException IOExceptions} are never thrown.
 */
public abstract class OverthereConnection {

	protected String type;

	protected OperatingSystemFamily os;

	protected int connectionTimeoutMillis;

	protected OverthereConnection(String type, ConnectionOptions options) {
		this.type = checkNotNull(type, "Cannot create HostConnection with null type");
		this.os = checkNotNull(options.<OperatingSystemFamily>get(OPERATING_SYSTEM), "Cannot create HostConnection with null os");
		this.connectionTimeoutMillis = options.get(CONNECTION_TIMEOUT_MILLIS, CONNECTION_TIMEOUT_MILLIS_DEFAULT);
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
	 * Closes the connection.
	 */
	public abstract void disconnect();

	/**
	 * Creates a reference to a file on the host.
	 * 
	 * @param hostPath
	 *            the path of the host
	 * @return a reference to the file
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
	 */
	public abstract OverthereFile getFile(OverthereFile parent, String child);

	/**
	 * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically removed when this connection is closed.
	 * <b>N.B.:</b> The file is not actually created until a put method is invoked.
	 * 
	 * @param nameTemplate
	 *            the template on which to base the name of the temporary file. May be <code>null</code>.
	 * @return a reference to the temporary file on the host
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
	 */
	public abstract OverthereFile getTempFile(String prefix, String suffix);

	/**
	 * Executes a command with its arguments.
	 * 
	 * @param handler
	 *            the handler that will be invoked when the executed command generated output.
	 * @param commandLine
	 *            the command line to execute.
	 * @return the exit value of the executed command. Usually 0 on successful execution.
	 */
	public final int execute(final OverthereProcessOutputHandler handler, final CmdLine commandLine) {
		final OverthereProcess process = startProcess(commandLine);
		Thread stdoutReaderThread = null;
		Thread stderrReaderThread = null;
		try {
			stdoutReaderThread = new Thread("Stdout reader thread for command " + commandLine + " on " + this) {
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

			stderrReaderThread = new Thread("Stderr reader thread for command " + commandLine + " on " + this) {
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

			try {
				return process.waitFor();
			} catch (InterruptedException exc) {
				Thread.currentThread().interrupt();
				throw new RuntimeIOException("Execution interrupted", exc);
			}
		} finally {
			process.destroy();

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
	 * Starts a command with its argument and returns control to the caller.
	 * 
	 * @param commandLine
	 *            the command line to execute.
	 * @return an object representing the executing command or <tt>null</tt> if this is not supported by the host connection.
	 */
	public abstract OverthereProcess startProcess(CmdLine commandLine);

	/**
	 * Subclasses MUST implement toString properly.
	 */
	@Override
	public abstract String toString();

	private static Logger logger = LoggerFactory.getLogger(OverthereConnection.class);

}
