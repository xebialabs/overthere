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

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.io.Closeables.closeQuietly;

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

/**
 * An input stream from a file on a host connected through SSH w/ SCP.
 */
class SshScpInputStream extends InputStream {

	protected SshScpOverthereFile file;

	protected SshScpOverthereConnection connection;

	protected Session session;

	protected ChannelExec channel;

	protected String command;

	protected InputStream channelIn;

	protected OutputStream channelOut;

	protected long bytesRemaining;

	SshScpInputStream(SshScpOverthereFile file) {
		this.file = file;

		try {
			// connect to SSH and start scp in source mode
			if(logger.isDebugEnabled())
				logger.debug("Connecting to " + connection + " (to run scp command to read from file " + file + ")");
			connection = (SshScpOverthereConnection) file.getConnection();
			session = connection.openSession();

			channel = (ChannelExec) session.openChannel("exec");
			CmdLine scpCommandLine = CmdLine.build("scp", "-f", file.getPath());
			command = scpCommandLine.toCommandLine(connection.getHostOperatingSystem(), false);
			channel.setCommand(command);
			channelIn = channel.getInputStream();
			channelOut = channel.getOutputStream();
			channel.connect();
			logger.info("Executing remote command \"" + scpCommandLine + "\" on " + connection + " to open SCP stream for reading");

			// perform SCP read protocol
			sendAck();
			readAck('C');
			readPermissions();
			bytesRemaining = readFileLength();
			readFilename();
			sendAck();
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
		checkState(channel != null && session != null, "Cannot close SCP input stream that has already been closed");

		try {
			readAck(0);
			sendAck();
		} catch (IOException ignore) {
		} catch (RuntimeIOException ignore2) {
		}
		closeQuietly(channelIn);
		closeQuietly(channelOut);
		channel.disconnect();
		channel = null;

		connection.disconnectSession(session);
		session = null;

		if(logger.isDebugEnabled())
			logger.debug("Disconnected from " + connection + " (to run scp command to read from file " + file + ")");
		logger.info("Closed SCP input stream to read from file " + file);
	}

	private Logger logger = LoggerFactory.getLogger(SshScpInputStream.class);

}
