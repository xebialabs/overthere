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
package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.Overthere.DEFAULT_CONNECTION_TIMEOUT_MS;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import jcifs.smb.SmbFile;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

/**
 * <ul>
 * <li>Windows Telnet Service in stream mode:<br/>
 * <tt>&gt; tlntadmn config mode=stream</tt></li>
 * <li>Shares with names like C$ are available for all drives you access.</li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
@Protocol(name = "cifs_telnet")
public class CifsTelnetConnection extends OverthereConnection implements OverthereConnectionBuilder {

	private String address;

	private String username;

	private String password;

	private static final String DETECTABLE_WINDOWS_PROMPT = "WINDOWS4DEPLOYIT ";

	private static final String ERRORLEVEL_PREAMBLE = "ERRORLEVEL-PREAMBLE";

	private static final String ERRORLEVEL_POSTAMBLE = "ERRORLEVEL-POSTAMBLE";

	/**
	 * The exitcode returned when the errorlevel of the Windows command could not be determined.
	 */
	public static final int EXITCODE_CANNOT_DETERMINE_ERRORLEVEL = -999999;

	public CifsTelnetConnection(String type, ConnectionOptions options) {
		super(type, options);
		this.address = options.get(ADDRESS);
		this.username = options.get(USERNAME);
		this.password = options.get(PASSWORD);
	}

	@Override
	public OverthereConnection connect() {
		return this;
	}

	@Override
	public OverthereFile getFile(String hostPath) throws RuntimeIOException {
		try {
			SmbFile smbFile = new SmbFile(encodeAsSmbUrl(hostPath));
			return new CifsFile(this, smbFile);
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	@Override
	public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
		StringBuilder childPath = new StringBuilder();
		childPath.append(parent.getPath());
		if(!parent.getPath().endsWith(getHostOperatingSystem().getFileSeparator())) {
			childPath.append(getHostOperatingSystem().getFileSeparator());
		}
		childPath.append(child.replace('\\', '/'));
		return getFile(childPath.toString());
	}

	@Override
	public OverthereProcess startProcess(final CmdLine commandLine) {
		final String commandLineForExecution = commandLine.toCommandLine(getHostOperatingSystem(), false);
		final String commandLineForLogging = commandLine.toCommandLine(getHostOperatingSystem(), true);

		try {
			final TelnetClient tc = new TelnetClient();
			tc.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT_MS);
			tc.addOptionHandler(new WindowSizeOptionHandler(299, 25, true, false, true, false));
			if (logger.isDebugEnabled()) {
				logger.debug("Connecting to telnet://" + username + "@" + address);
			}
			tc.connect(address);
			logger.info("Connected to telnet://" + username + "@" + address);
			final InputStream stdout = tc.getInputStream();
			final OutputStream stdin = tc.getOutputStream();
			final PipedInputStream callersStdout = new PipedInputStream();
			final PipedOutputStream toCallersStdout = new PipedOutputStream(callersStdout);
			
			final StringBuilder outputBuf = new StringBuilder();

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

			send(stdin, commandLineForExecution);

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
							receive(stdout, outputBuf, toCallersStdout, DETECTABLE_WINDOWS_PROMPT);

							send(stdin, "ECHO \"" + ERRORLEVEL_PREAMBLE + "%errorlevel%" + ERRORLEVEL_POSTAMBLE);
							receive(stdout, outputBuf, toCallersStdout, ERRORLEVEL_POSTAMBLE);
							receive(stdout, outputBuf, toCallersStdout, ERRORLEVEL_POSTAMBLE);
							int preamblePos = outputBuf.indexOf(ERRORLEVEL_PREAMBLE);
							int postamblePos = outputBuf.indexOf(ERRORLEVEL_POSTAMBLE);
							if (preamblePos >= 0 && postamblePos >= 0) {
								String errorlevelString = outputBuf.substring(preamblePos + ERRORLEVEL_PREAMBLE.length(), postamblePos);
								if (logger.isDebugEnabled())
									logger.debug("Errorlevel string found: " + errorlevelString);

								try {
									return Integer.parseInt(errorlevelString);
								} catch (NumberFormatException exc) {
									logger.error("Cannot parse errorlevel in Windows output: " + outputBuf);
									return EXITCODE_CANNOT_DETERMINE_ERRORLEVEL;
								}
							} else {
								logger.error("Cannot find errorlevel in Windows output: " + outputBuf);
								return EXITCODE_CANNOT_DETERMINE_ERRORLEVEL;
							}
						} finally {
							destroy();
						}
					} catch (IOException exc) {
						throw new RuntimeIOException("Cannot execute command " + commandLineForLogging + " on " + address, exc);
					}
				}

				@Override
				public void destroy() {
					if (tc.isConnected()) {
						try {
							tc.disconnect();
							logger.info("Disconnected from telnet://" + username + "@" + address);

							toCallersStdout.close();
						} catch (IOException exc) {
							throw new RuntimeIOException("Cannot disconnect from telnet://" + username + "@" + address, exc);
						}
					}
				}
			};
		} catch (InvalidTelnetOptionException exc) {
			throw new RuntimeIOException("Cannot execute command " + commandLineForLogging + " at telnet://" + username + "@" + address, exc);
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot execute command " + commandLineForLogging + " at telnet://" + username + "@" + address, exc);
		}
	}

	private void receive(final InputStream stdout, final StringBuilder outputBuf, final PipedOutputStream toCallersStdout, final String expectedString) throws IOException {
		receive(stdout, outputBuf, toCallersStdout, expectedString, null);
	}

	private void receive(final InputStream stdout, final StringBuilder outputBuf, final PipedOutputStream toCallersStdout, final String expectedString,
	        final String unexpectedString) throws IOException {
		boolean lastCharWasCr = false;
		boolean lastCharWasEsc = false;
		for (;;) {
			int c = stdout.read();
			if (c == -1) {
				throw new IOException("End of stream reached");
			}

			toCallersStdout.write(c);
			switch ((char) c) {
			case '\r':
				outputBuf.delete(0, outputBuf.length());
				break;
			case '\n':
				if (!lastCharWasCr) {
					outputBuf.delete(0, outputBuf.length());
				}
				break;
			case '[':
				if (lastCharWasEsc) {
					throw new RuntimeIOException(
					        "VT100/ANSI escape sequence found in output stream. Please configure the Windows Telnet server to use stream mode (tlntadmn config mode=stream).");
				}
			default:
				outputBuf.append((char) c);
				break;
			}
			lastCharWasCr = (c == '\r');
			lastCharWasEsc = (c == 27);

			if (unexpectedString != null && outputBuf.length() >= unexpectedString.length()) {
				String s = outputBuf.substring(outputBuf.length() - unexpectedString.length(), outputBuf.length());
				if (s.equals(unexpectedString)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Unexpected string \"" + unexpectedString + "\" found in Windows Telnet output");
					}
					throw new IOException("Unexpected string \"" + unexpectedString + "\" found in Windows Telnet output");
				}
			}

			if (outputBuf.length() >= expectedString.length()) {
				String s = outputBuf.substring(outputBuf.length() - expectedString.length(), outputBuf.length());
				if (s.equals(expectedString)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Expected string \"" + expectedString + "\" found in Windows Telnet output");
					}
					return;
				}
			}
		}
	}

	private void send(OutputStream stdin, String lineToSend) throws IOException {
		byte[] bytesToSend = (lineToSend + "\r\n").getBytes();
		stdin.write(bytesToSend);
		stdin.flush();
	}

	// FIXME: Move to OverthereConnectionUtils
	public OverthereFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
		if (prefix == null)
			throw new NullPointerException("prefix is null");

		if (suffix == null) {
			suffix = ".tmp";
		}

		Random r = new Random();
		String infix = "";
		for (int i = 0; i < MAX_TEMP_RETRIES; i++) {
			OverthereFile f = getFile(getTempDirectory().getPath() + getHostOperatingSystem().getFileSeparator() + prefix + infix + suffix);
			if (!f.exists()) {
				if (logger.isDebugEnabled())
					logger.debug("Created temporary file " + f);

				return f;
			}
			infix = "-" + Long.toString(Math.abs(r.nextLong()));
		}
		throw new RuntimeIOException("Cannot generate a unique temporary file name on " + this);
	}

	private String encodeAsSmbUrl(String hostPath) {
		StringBuffer smbUrl = new StringBuffer();
		smbUrl.append("smb://");
		smbUrl.append(urlEncode(username.replaceFirst("\\\\", ";")));
		smbUrl.append(":");
		smbUrl.append(urlEncode(password));
		smbUrl.append("@");
		smbUrl.append(urlEncode(address));
		smbUrl.append("/");

		if (hostPath.length() < 2) {
			throw new RuntimeIOException("Host path \"" + hostPath + "\" is too short");
		}

		if (hostPath.charAt(1) != ':') {
			throw new RuntimeIOException("Host path \"" + hostPath + "\" does not have a colon (:) as its second character");
		}
		smbUrl.append(hostPath.charAt(0));
		smbUrl.append("$/");
		if (hostPath.length() >= 3) {
			if (hostPath.charAt(2) != '\\') {
				throw new RuntimeIOException("Host path \"" + hostPath + "\" does not have a backslash (\\) as its third character");
			}
			smbUrl.append(hostPath.substring(3).replace('\\', '/'));
		}

		logger.trace("Encoded Windows host path {} to SMB URL {}", hostPath, smbUrl.toString());

		return smbUrl.toString();
	}

	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeIOException("Unable to construct SMB URL", e);
		}
	}

	public String toString() {
		return "cifs_telnet://" + username + "@" + address;
	}

	private static Logger logger = LoggerFactory.getLogger(CifsTelnetConnection.class);

}
