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