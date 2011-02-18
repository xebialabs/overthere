/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.overthere.ssh;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;

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