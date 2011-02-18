package com.xebialabs.overthere.cifs;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.xebialabs.overthere.Overthere.DEFAULT_CONNECTION_TIMEOUT_MS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.common.AbstractHostConnection;
import jcifs.smb.SmbFile;

import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/**
 * <ul>
 * <li>Windows Telnet Service in stream mode:<br/>
 * <tt>&gt; tlntadmn config mode=stream</tt></li>
 * <li>Shares with names like C$ are available for all drives you access.</li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public class CifsTelnetHostConnection extends AbstractHostConnection implements HostConnection {

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

	public CifsTelnetHostConnection(String type, ConnectionOptions options) {
		super(type, options);
		this.address = options.get("address");
		this.username = options.get("username");
		this.password = options.get("password");
	}

	@SuppressWarnings("unchecked")
	public int execute(CommandExecutionCallbackHandler handler, String... commandLine) throws RuntimeIOException {
		return execute(handler, Collections.EMPTY_MAP, commandLine);
	}

	public int execute(CommandExecutionCallbackHandler handler, Map<String, String> inputResponse, String... cmdarray) {
		String commandLineForExecution = encodeCommandLineForExecution(cmdarray);
		String commandLineForLogging = encodeCommandLineForLogging(cmdarray);

		try {
			TelnetClient tc = new TelnetClient();
			tc.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT_MS);
			tc.addOptionHandler(new WindowSizeOptionHandler(299, 25, true, false, true, false));
			tc.connect(address);
			try {
				InputStream stdout = tc.getInputStream();
				OutputStream stdin = tc.getOutputStream();
				StringBuffer outputBuf = new StringBuffer();

				waitFor(handler, stdout, outputBuf, "ogin:");
				send(handler, stdin, username);

				waitFor(handler, stdout, outputBuf, "assword:");
				send(handler, stdin, password);

				waitFor(handler, stdout, outputBuf, ">", "ogon failure");
				send(handler, stdin, "PROMPT " + DETECTABLE_WINDOWS_PROMPT);
				// We must wait for the prompt twice; the first time is an echo of the PROMPT command,
				// the second is the actual prompt
				waitFor(handler, stdout, outputBuf, DETECTABLE_WINDOWS_PROMPT);
				waitFor(handler, stdout, outputBuf, DETECTABLE_WINDOWS_PROMPT);

				send(handler, stdin, commandLineForExecution);
				waitFor(handler, stdout, outputBuf, DETECTABLE_WINDOWS_PROMPT);

				send(handler, stdin, "ECHO \"" + ERRORLEVEL_PREAMBLE + "%errorlevel%" + ERRORLEVEL_POSTAMBLE);
				waitFor(handler, stdout, outputBuf, ERRORLEVEL_POSTAMBLE);
				waitFor(handler, stdout, outputBuf, ERRORLEVEL_POSTAMBLE);
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
				tc.disconnect();
			}
		} catch (InvalidTelnetOptionException exc) {
			throw new RuntimeIOException("Cannot execute command " + commandLineForLogging + " on " + address, exc);
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot execute command " + commandLineForLogging + " on " + address, exc);
		}
	}

	private void waitFor(final CommandExecutionCallbackHandler handler, final InputStream stdout, final StringBuffer outputBuf, final String expectedString)
	        throws IOException {
		waitFor(handler, stdout, outputBuf, expectedString, null);
	}

	private void waitFor(final CommandExecutionCallbackHandler handler, final InputStream stdout, final StringBuffer outputBuf, final String expectedString,
	        final String unexpectedString) throws IOException {
		boolean lastCharWasCr = false;
		boolean lastCharWasEsc = false;
		for (;;) {
			int c = stdout.read();
			if (c == -1) {
				throw new IOException("End of stream reached");
			}

			handler.handleOutput((char) c);
			switch ((char) c) {
			case '\r':
				handler.handleOutputLine(outputBuf.toString());
				outputBuf.delete(0, outputBuf.length());
				break;
			case '\n':
				if (!lastCharWasCr) {
					handler.handleOutputLine(outputBuf.toString());
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

	private void send(CommandExecutionCallbackHandler handler, OutputStream stdin, String lineToSend) throws IOException {
		byte[] bytesToSend = (lineToSend + "\r\n").getBytes();
		stdin.write(bytesToSend);
		stdin.flush();
	}

	public CommandExecution startExecute(String... commandLine) {
		return null;
	}

	public HostFile getFile(String hostPath) throws RuntimeIOException {
		try {
			SmbFile smbFile = new SmbFile(encodeAsSmbUrl(hostPath));
			return new CifsHostFile(this, smbFile);
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public HostFile getFile(HostFile parent, String child) throws RuntimeIOException {
		return getFile(parent.getPath() + getHostOperatingSystem().getFileSeparator() + child.replace('\\', '/'));
	}

	public HostFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
		checkNotNull(prefix);
		if (suffix == null) {
			suffix = ".tmp";
		}

		Random r = new Random();
		String infix = "";
		for (int i = 0; i < AbstractHostConnection.MAX_TEMP_RETRIES; i++) {
			HostFile f = getFile(getTemporaryDirectory().getPath() + getHostOperatingSystem().getFileSeparator() + prefix + infix + suffix);
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

		if (logger.isDebugEnabled())
			logger.debug("Encoded Windows host path \"" + hostPath + "\" to SMB URL \"" + smbUrl.toString() + "\"");
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
		return username + "@" + address;
	}

	private static Logger logger = LoggerFactory.getLogger(CifsTelnetHostConnection.class);

}
