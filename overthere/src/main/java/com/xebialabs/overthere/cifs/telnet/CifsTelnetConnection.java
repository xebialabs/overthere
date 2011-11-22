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
package com.xebialabs.overthere.cifs.telnet;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.cifs.CifsConnection;

/**
 * A connection to a remote host using CIFS and Telnet.
 * 
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator access is needed.</li>
 * <li>Windows Telnet Service must be configured to use stream mode:<br/>
 * <tt>&gt; tlntadmn config mode=stream</tt></li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public class CifsTelnetConnection extends CifsConnection {

	private static final String DETECTABLE_WINDOWS_PROMPT = "WINDOWS4DEPLOYIT ";

	private static final String ERRORLEVEL_PREAMBLE = "ERRORLEVEL-PREAMBLE";

	private static final String ERRORLEVEL_POSTAMBLE = "ERRORLEVEL-POSTAMBLE";

	/**
	 * The exitcode returned when the errorlevel of the Windows command could not be determined.
	 */
	public static final int EXITCODE_CANNOT_DETERMINE_ERRORLEVEL = -999999;

	/**
	 * Creates a {@link CifsTelnetConnection}. Don't invoke directly. Use {@link Overthere#getConnection(String, ConnectionOptions)} instead.
	 */
	public CifsTelnetConnection(String type, ConnectionOptions options) {
		super(type, options, true);
		checkArgument(os == WINDOWS, "Cannot start a " + CIFS_PROTOCOL + ":%s connection to a non-Windows operating system", cifsConnectionType.toString().toLowerCase());
	}

	@Override
	public OverthereProcess startProcess(final CmdLine commandLine) {
		final String commandLineForExecution = commandLine.toCommandLine(getHostOperatingSystem(), false);

		try {
			final TelnetClient tc = new TelnetClient();
			tc.setConnectTimeout(connectionTimeoutMillis);
			tc.addOptionHandler(new WindowSizeOptionHandler(299, 25, true, false, true, false));
			logger.info("Connecting to telnet://{}@{}", username, address);
			tc.connect(address, port);
			final InputStream stdout = tc.getInputStream();
			final OutputStream stdin = tc.getOutputStream();
			final PipedInputStream callersStdout = new PipedInputStream();
			final PipedOutputStream toCallersStdout = new PipedOutputStream(callersStdout);
			final ByteArrayOutputStream outputBuf = new  ByteArrayOutputStream();
			final int[] result = new int[1];
			result[0] = EXITCODE_CANNOT_DETERMINE_ERRORLEVEL;

			final Thread processOutputReaderThread = new Thread("Process handler reader for command " + commandLine) {
				@Override
				public void run() {
					try {
						receive(stdout, outputBuf, toCallersStdout, "ogin:");
						send(stdin, username);
	
						receive(stdout, outputBuf, toCallersStdout, "assword:");
						send(stdin, password);
	
						receive(stdout, outputBuf, toCallersStdout, ">", "ogon failure");
						send(stdin, "PROMPT " + DETECTABLE_WINDOWS_PROMPT);
						// We must wait for the prompt twice; the first time is an echo of the PROMPT command,
						// the second is the actual prompt
						receive(stdout, outputBuf, toCallersStdout, DETECTABLE_WINDOWS_PROMPT);
						receive(stdout, outputBuf, toCallersStdout, DETECTABLE_WINDOWS_PROMPT);

						if(workingDirectory != null) {
							send(stdin, "CD " + workingDirectory.getPath());
							receive(stdout, outputBuf, toCallersStdout, DETECTABLE_WINDOWS_PROMPT);
						}

						send(stdin, commandLineForExecution);
	
						receive(stdout, outputBuf, toCallersStdout, DETECTABLE_WINDOWS_PROMPT);
	
						send(stdin, "ECHO \"" + ERRORLEVEL_PREAMBLE + "%errorlevel%" + ERRORLEVEL_POSTAMBLE);
						receive(stdout, outputBuf, toCallersStdout, ERRORLEVEL_POSTAMBLE);
						receive(stdout, outputBuf, toCallersStdout, ERRORLEVEL_POSTAMBLE);
						String outputBufStr = outputBuf.toString();
						int preamblePos = outputBufStr.indexOf(ERRORLEVEL_PREAMBLE);
						int postamblePos = outputBufStr.indexOf(ERRORLEVEL_POSTAMBLE);
						if (preamblePos >= 0 && postamblePos >= 0) {
							String errorlevelString = outputBufStr.substring(preamblePos + ERRORLEVEL_PREAMBLE.length(), postamblePos);
							if (logger.isDebugEnabled())
								logger.debug("Errorlevel string found: " + errorlevelString);
	
							try {
								result[0] =  Integer.parseInt(errorlevelString);
							} catch (NumberFormatException exc) {
								logger.error("Cannot parse errorlevel in Windows output: " + outputBuf);
							}
						} else {
							logger.error("Cannot find errorlevel in Windows output: " + outputBuf);
						}
					} catch(IOException exc) {
						throw new RuntimeIOException("Cannot start process " + commandLine, exc);
					} finally {
						Closeables.closeQuietly(toCallersStdout);
					}
				}
			};
			processOutputReaderThread.start();
			
			return new OverthereProcess() {
				@Override
				public OutputStream getStdin() {
					return stdin;
				}

				@Override
				public InputStream getStdout() {
					return callersStdout;
				}

				@Override
				public InputStream getStderr() {
					return new ByteArrayInputStream(new byte[0]);
				}

				@Override
				public int waitFor() {
					try {
						try {
							processOutputReaderThread.join();
						} finally {
							destroy();
						}
						return result[0];
					} catch (InterruptedException exc) {
						throw new RuntimeIOException("Cannot execute command " + commandLine + " on " + address, exc);
					}
				}

				@Override
				public void destroy() {
					if (tc.isConnected()) {
						try {
							tc.disconnect();
							logger.info("Disconnected from telnet://{}@{}", username, address);

							Closeables.closeQuietly(toCallersStdout);
						} catch (IOException exc) {
							throw new RuntimeIOException("Cannot disconnect from telnet://" + username + "@" + address, exc);
						}
					}
				}
			};
		} catch (InvalidTelnetOptionException exc) {
			throw new RuntimeIOException("Cannot execute command " + commandLine + " at telnet://" + username + "@" + address, exc);
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot execute command " + commandLine + " at telnet://" + username + "@" + address, exc);
		}
	}

	private void receive(final InputStream stdout, final ByteArrayOutputStream outputBuf, final PipedOutputStream toCallersStdout, final String expectedString) throws IOException {
		receive(stdout, outputBuf, toCallersStdout, expectedString, null);
	}

	private void receive(final InputStream stdout, final ByteArrayOutputStream outputBuf, final PipedOutputStream toCallersStdout, final String expectedString, final String unexpectedString) throws IOException {
		boolean lastCharWasCr = false;
		boolean lastCharWasEsc = false;
		for (;;) {
			int cInt = stdout.read();
			if (cInt == -1) {
				throw new IOException("End of stream reached");
			}

			outputBuf.write(cInt);
			final String outputBufStr = outputBuf.toString();
			char c = (char) cInt;
			switch (c) {
			case '\r':
				handleReceivedLine(outputBuf, outputBufStr, toCallersStdout);
				break;
			case '\n':
				if (!lastCharWasCr) {
					handleReceivedLine(outputBuf, outputBufStr, toCallersStdout);
				}
				break;
			case '[':
				if (lastCharWasEsc) {
					throw new RuntimeIOException(
					        "VT100/ANSI escape sequence found in output stream. Please configure the Windows Telnet server to use stream mode (tlntadmn config mode=stream).");
				}
			}
			lastCharWasCr = (c == '\r');
			lastCharWasEsc = (c == 27);

			if (unexpectedString != null && outputBufStr.length() >= unexpectedString.length()) {
				String s = outputBufStr.substring(outputBufStr.length() - unexpectedString.length(), outputBufStr.length());
				if (s.equals(unexpectedString)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Unexpected string \"" + unexpectedString + "\" found in Windows Telnet output");
					}
					throw new IOException("Unexpected string \"" + unexpectedString + "\" found in Windows Telnet output");
				}
			}

			if (outputBufStr.length() >= expectedString.length()) {
				String s = outputBufStr.substring(outputBufStr.length() - expectedString.length(), outputBufStr.length());
				if (s.equals(expectedString)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Expected string \"" + expectedString + "\" found in Windows Telnet output");
					}
					return;
				}
			}
		}
	}

	private void handleReceivedLine(final ByteArrayOutputStream outputBuf, final String outputBufStr, final PipedOutputStream toCallersStdout) throws IOException {
		if(!outputBufStr.contains(DETECTABLE_WINDOWS_PROMPT)) {
			toCallersStdout.write(outputBuf.toByteArray());
		    toCallersStdout.flush();
		}
		outputBuf.reset();
    }

	private void send(final OutputStream stdin, final String lineToSend) throws IOException {
		byte[] bytesToSend = (lineToSend + "\r\n").getBytes();
		stdin.write(bytesToSend);
		stdin.flush();
	}

	private static Logger logger = LoggerFactory.getLogger(CifsTelnetConnection.class);

}
