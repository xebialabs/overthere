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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SshInteractiveSudoPasswordHandlingStream extends FilterInputStream {
	private final OutputStream remoteStdin;
	private final byte[] passwordBytes;
	
	private final StringBuilder receivedOutputBuffer = new StringBuilder();
	private boolean onFirstLine = true;
	
	protected SshInteractiveSudoPasswordHandlingStream(InputStream remoteStdout, OutputStream remoteStdin, String password) {
		super(remoteStdout);
		this.remoteStdin = remoteStdin;
		passwordBytes = (password + "\r\n").getBytes();
	}

	@Override
	public int read() throws IOException {
		int readInt = super.read();
		if (readInt > -1) {
			handleChar((char) readInt);
		}
		return readInt;
	}
	
	private void handleChar(char c) {
		if (onFirstLine) {
			switch (c) {
			case ':':
				String receivedOutput = receivedOutputBuffer.toString();
				if (receivedOutput.endsWith("assword")) {
					if (logger.isDebugEnabled()) {
						logger.debug("Found password prompt in first line of output: " + receivedOutput);
					}
					try {
						remoteStdin.write(passwordBytes);
						remoteStdin.flush();
						if (logger.isDebugEnabled()) {
							logger.debug("Sent password");
						}
					} catch (IOException exc) {
						logger.error("Cannot send password", exc);
					}
				}
				break;
			case '\n':
				onFirstLine = false;
				break;
			default:
				receivedOutputBuffer.append(c);
				break;
			}
		} 
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
	
	private static Logger logger = LoggerFactory.getLogger(SshInteractiveSudoPasswordHandlingStream.class);
}

