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
import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;

/**
 * An output stream to a file on a host connected through SSH w/ SCP.
 */
class SshScpOutputStream extends OutputStream {

	protected SshScpHostFile file;

	protected long length;

	protected String command;

	protected Session session;

	protected ChannelExec channel;

	protected InputStream channelIn;

	protected OutputStream channelOut;

	private static final String CHANNEL_PURPOSE = " (for SCP input stream)";

	SshScpOutputStream(SshScpHostFile file, long length) {
		this.file = file;
		this.length = length;
	}

	void open() {
		try {
			// connect to SSH and start scp in sink mode
			session = file.sshHostSession.openSession(CHANNEL_PURPOSE);
			channel = (ChannelExec) session.openChannel("exec");
			// no password in this command, so use 'false'
			command = file.sshHostSession.encodeCommandLineForExecution("scp", "-t", file.remotePath);
			channel.setCommand(command);
			channelIn = channel.getInputStream();
			channelOut = channel.getOutputStream();
			channel.connect();
			logger.info("Executing remote command \"" + command + "\" on " + file.sshHostSession + " to open SCP stream for writing");

			// perform SCP write protocol
			readAck();
			sendFilePreamble();
			logger.info("Opened SCP stream to write to remote file " + file);
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot open SCP stream to write remote file " + file, exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot open SCP stream to write remote file " + file, exc);
		}
	}

	private void readAck() {
		if (logger.isDebugEnabled())
			logger.debug("Reading ACK");

		int c = SshStreamUtils.checkAck(channelIn);
		if (c != 0) {
			throw new RuntimeIOException("Protocol error on SCP stream to write remote file " + file);
		}
	}

	private void sendFilePreamble() throws IOException {
		String preamble = "C0644 " + length + " ";
		if (file.remotePath.lastIndexOf('/') > 0) {
			preamble += file.remotePath.substring(file.remotePath.lastIndexOf('/') + 1);
		} else {
			preamble += file.remotePath;
		}
		if (logger.isDebugEnabled())
			logger.debug("Sending file preamble \"" + preamble + "\"");
		preamble += "\n";

		channelOut.write(preamble.getBytes());
		channelOut.flush();
	}

	private void sendAck() throws IOException {
		if (logger.isDebugEnabled())
			logger.debug("Sending ACK");

		channelOut.write('\0');
		channelOut.flush();
	}

	@Override
	public void write(byte[] b) throws IOException {
		channelOut.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		channelOut.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		channelOut.write(b);
	}

	@Override
	public void close() {
		try {
			sendAck();
			readAck();
		} catch (IOException ignore) {
		}

		// close output channel to force remote scp to quit
		IOUtils.closeQuietly(channelOut);

		// get return code from remote scp
		int res = SshHostSession.waitForExitStatus(channel, command);

		IOUtils.closeQuietly(channelIn);
		channel.disconnect();
		file.sshHostSession.disconnectSession(session, CHANNEL_PURPOSE);
		if (res != 0) {
			throw new RuntimeIOException("Error closing SCP stream to write remote file " + file + " (remote scp command returned error code " + res + ")");
		}

		chmodWrittenFile();
	}

	private void chmodWrittenFile() {
		if (file.sshHostSession instanceof SshSudoHostSession) {
			SshSudoHostSession sshSudoHostSession = (SshSudoHostSession) file.sshHostSession;
			CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
			int errno = sshSudoHostSession.noSudoExecute(capturedOutput, "chmod", "0666", file.remotePath);
			if (errno != 0) {
				throw new RuntimeIOException("Cannot chmod file " + file + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
			}
			if (logger.isDebugEnabled())
				logger.debug("Chmodded file " + file);
		}
	}

	private Logger logger = LoggerFactory.getLogger(SshScpOutputStream.class);

}
