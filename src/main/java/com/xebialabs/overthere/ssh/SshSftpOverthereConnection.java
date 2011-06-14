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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.Protocol;

/**
 * A connection to a remote host using SSH w/ SFTP.
 */
@Protocol(name = "ssh_sftp")
public class SshSftpOverthereConnection extends SshOverthereConnection {

	private ChannelSftp sharedSftpChannel;

	public SshSftpOverthereConnection(String type, ConnectionOptions options) {
		super(type, options);
	}

	@Override
	public OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
		return new SshSftpOverthereFile(this, hostPath);
	}

	@Override
	public void disconnect() {
		super.disconnect();

		if (sharedSftpChannel != null) {
			closeSftpChannel(sharedSftpChannel, true);
		}
	}

	protected ChannelSftp getSharedSftpChannel() throws JSchException {
		if (sharedSftpChannel == null) {
			sharedSftpChannel = openSftpChannel(true);
		}
		return sharedSftpChannel;
	}

	protected ChannelSftp openSftpChannel(boolean shared) throws JSchException {
		Channel channel = getSharedSession().openChannel("sftp");
		if (logger.isDebugEnabled())
			logger.debug((shared ? "Opening shared SFTP channel to " : "Opening SFTP channel to ") + this);
		channel.connect();
		return (ChannelSftp) channel;
	}

	protected void closeSftpChannel(ChannelSftp sftpChannel, boolean shared) {
		if (sftpChannel != null) {
			if (logger.isDebugEnabled())
				logger.debug((shared ? "Closing shared SFTP channel to " : "Closing SFTP channel to ") + this);
			sftpChannel.disconnect();
		}
	}

	private Logger logger = LoggerFactory.getLogger(SshSftpOverthereConnection.class);

}
