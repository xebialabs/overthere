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
package com.xebialabs.overthere.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * Detects password prompts in the output stream and sends the password in response.
 */
class SshInteractiveSudoPasswordHandlingStream extends FilterInputStream {
	private final OutputStream remoteStdin;
	private final byte[] passwordBytes;
	private final String passwordRegex;
	private final Pattern passwordPattern;

	private final StringBuilder receivedOutputBuffer = new StringBuilder();

	private boolean onFirstLine = true;

	protected SshInteractiveSudoPasswordHandlingStream(InputStream remoteStdout, OutputStream remoteStdin, String password, String passwordPromptRegex) {
		super(remoteStdout);
		this.remoteStdin = remoteStdin;
		this.passwordBytes = (password + "\r\n").getBytes();

		this.passwordRegex = passwordPromptRegex;
		this.passwordPattern = Pattern.compile(passwordRegex);
	}

	@Override
	public int read() throws IOException {
		int readInt = super.read();
		if (readInt > -1) {
			handleChar((char) readInt);
		}
		return readInt;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int numBytesRead = super.read(b, off, len);
		if (numBytesRead > -1) {
			for (int i = 0; i < numBytesRead; i++) {
				handleChar((char) b[off + i]);
			}
		}
		return numBytesRead;
	}

	private void handleChar(char c) {
		if (onFirstLine) {
			logger.trace("Received: {}", c);
			if (c == '\n') {
				onFirstLine = false;
			} else {
				receivedOutputBuffer.append(c);

				if (c == passwordRegex.charAt(passwordRegex.length() - 1)) {
					String receivedOutput = receivedOutputBuffer.toString();
					if (passwordPattern.matcher(receivedOutput).matches()) {
						logger.info("Found password prompt in first line of output: {}", receivedOutput);
						try {
							remoteStdin.write(passwordBytes);
							remoteStdin.flush();
							logger.debug("Sent password");
						} catch (IOException exc) {
							logger.error("Cannot send password", exc);
						}
					}
				}
			}
		}
	}

	private static Logger logger = LoggerFactory.getLogger(SshInteractiveSudoPasswordHandlingStream.class);

}
