/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

