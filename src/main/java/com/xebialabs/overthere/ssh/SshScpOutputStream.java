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

import static com.xebialabs.overthere.CmdLine.build;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;
import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

/**
 * An output stream to a file on a host connected through SSH w/ SCP.
 */
class SshScpOutputStream extends OutputStream {

	protected SshScpOverthereFile file;

	protected SshScpOverthereConnection connection;

	protected long length;

	protected Session session;

	protected ChannelExec channel;

	protected String command;

	protected InputStream channelIn;

	protected OutputStream channelOut;

	SshScpOutputStream(SshScpOverthereFile file, long length) {
		this.file = file;
		this.length = length;
	}

	void open() {
		try {
			// connect to SSH and start scp in sink mode
			connection = (SshScpOverthereConnection) file.getConnection();
			session = connection.openSession();
			logger.info("Connected to " + connection);

			channel = (ChannelExec) session.openChannel("exec");
			CmdLine scpCommandLine = CmdLine.build("scp", "-t", file.getPath());
			command = scpCommandLine.toCommandLine(connection.getHostOperatingSystem(), false);
			channel.setCommand(command);
			channelIn = channel.getInputStream();
			channelOut = channel.getOutputStream();
			channel.connect();
			logger.info("Executing remote command \"" + command + "\" on " + connection + " to open SCP stream for writing");

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
		if (file.getPath().lastIndexOf('/') > 0) {
			preamble += file.getPath().substring(file.getPath().lastIndexOf('/') + 1);
		} else {
			preamble += file.getPath();
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
		closeQuietly(channelOut);

		// get return code from remote scp
		int res = SshOverthereConnection.waitForExitStatus(channel, command);

		closeQuietly(channelIn);
		channel.disconnect();
		connection.disconnectSession(session);
		logger.info("Disconnected from " + connection);

		if (res != 0) {
			throw new RuntimeIOException("Error closing SCP stream to write remote file " + file + " (remote scp command returned error code " + res + ")");
		}

		chmodWrittenFile();
	}

	private void chmodWrittenFile() {
		if (connection instanceof SshSudoOverthereConnection) {
			if(logger.isDebugEnabled()) {
				logger.debug("Chmodding 0666 " + file);
			}

			CapturingOverthereProcessOutputHandler capturedOutput = CapturingOverthereProcessOutputHandler.capturingHandler();
			int errno = ((SshSudoOverthereConnection) connection).noSudoExecute(multiHandler(loggingHandler(logger), capturedOutput), build("chmod", "0666", file.getPath()));
			if (errno != 0) {
				throw new RuntimeIOException("Cannot chmod file " + file + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
			}
			logger.info("Chmodded 0666 " + file);
		}
	}

	private Logger logger = LoggerFactory.getLogger(SshScpOutputStream.class);

}
