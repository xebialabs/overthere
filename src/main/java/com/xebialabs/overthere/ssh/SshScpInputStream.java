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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.xebialabs.deployit.exception.RuntimeIOException;

/**
 * An input stream from a file on a host connected through SSH w/ SCP.
 */
class SshScpInputStream extends InputStream {

	protected SshScpHostFile file;

	protected Session session;

	protected ChannelExec channel;

	protected InputStream channelIn;

	protected OutputStream channelOut;

	protected long bytesRemaining;

	private static final String CHANNEL_PURPOSE = " (for SCP input stream)";

	SshScpInputStream(SshScpHostFile file) {
		this.file = file;
	}

	void open() {
		try {
			// connect to SSH and start scp in source mode
			session = file.sshHostSession.openSession(CHANNEL_PURPOSE);
			channel = (ChannelExec) session.openChannel("exec");
			// no password in this command, so use 'false'
			String command = file.sshHostSession.encodeCommandLineForExecution("scp", "-f", file.remotePath);
			channel.setCommand(command);
			channelIn = channel.getInputStream();
			channelOut = channel.getOutputStream();
			channel.connect();
			logger.info("Executing remote command \"" + command + "\" on " + file.sshHostSession + " to open SCP stream for reading");

			// perform SCP read protocol
			sendAck();
			readAck('C');
			readPermissions();
			bytesRemaining = readFileLength();
			readFilename();
			sendAck();
			logger.info("Opened SCP stream to read from remote file " + file);
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot open SCP stream to read remote file " + file, exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot open SCP stream to read remote file " + file, exc);
		}
	}

	private void sendAck() throws IOException {
		if (logger.isDebugEnabled())
			logger.debug("Sending ACK");

		byte[] buf = new byte[1];
		buf[0] = 0;
		channelOut.write(buf);
		channelOut.flush();
	}

	private void readAck(int expectedChar) {
		if (logger.isDebugEnabled())
			logger.debug("Reading ACK");

		int c = SshStreamUtils.checkAck(channelIn);
		if (c != expectedChar) {
			throw new RuntimeIOException("Protocol error on SCP stream to read remote file " + file + " (remote scp command returned acked with a \"" + c + "\")");
		}
	}

	private void readPermissions() throws IOException {
		if (logger.isDebugEnabled())
			logger.debug("Reading permissions");

		// read '0644 '
		byte[] buf = new byte[5];
		channelIn.read(buf, 0, 5);
	}

	private long readFileLength() throws IOException {
		if (logger.isDebugEnabled())
			logger.debug("Reading file length");

		// read file length terminated by a space
		long filelength = 0L;
		while (true) {
			int c;
			if ((c = channelIn.read()) < 0) {
				throw new RuntimeIOException("Protocol error on SCP stream to read remote file " + file);
			}
			if (c == ' ') {
				if (logger.isDebugEnabled())
					logger.debug("File length = " + filelength);
				return filelength;
			}
			filelength = filelength * 10L + (long) (c - '0');
		}

	}

	private void readFilename() throws IOException {
		if (logger.isDebugEnabled())
			logger.debug("Reading file name");

		// read filename terminated by a newline
		while (true) {
			int c;
			if ((c = channelIn.read()) < 0) {
				throw new RuntimeIOException("Protocol error on SCP stream to read remote file " + file);
			}
			if (c == '\n') {
				break;
			}
		}
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (bytesRemaining > 0) {
			int bytesRead = channelIn.read(b, off, (int) Math.min(len, bytesRemaining));
			bytesRemaining -= bytesRead;
			return bytesRead;
		} else {
			return -1;
		}
	}

	@Override
	public int read() throws IOException {
		if (bytesRemaining > 0) {
			int b = channelIn.read();
			if (b >= 0) {
				bytesRemaining--;
			}
			return b;
		} else {
			return -1;
		}
	}

	@Override
	public void close() {
		try {
			readAck(0);
			sendAck();
		} catch (IOException ignore) {
		} catch(RuntimeIOException ignore2) {
		}
		IOUtils.closeQuietly(channelIn);
		IOUtils.closeQuietly(channelOut);
		channel.disconnect();
		file.sshHostSession.disconnectSession(session, CHANNEL_PURPOSE);
	}

	private Logger logger = LoggerFactory.getLogger(SshScpInputStream.class);

}
