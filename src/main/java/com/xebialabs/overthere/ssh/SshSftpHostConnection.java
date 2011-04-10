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

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Protocol;
import com.xebialabs.overthere.RuntimeIOException;
import org.slf4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.xebialabs.overthere.OverthereFile;
import org.slf4j.LoggerFactory;

/**
 * A connection to a remote host using SSH w/ SFTP.
 */
@Protocol(name = "ssh_sftp")
public class SshSftpHostConnection extends SshHostConnection {

	private ChannelSftp sharedSftpChannel;

	public SshSftpHostConnection(String type, ConnectionOptions options) {
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
			closeSftpChannel(sharedSftpChannel);
		}
	}

	protected ChannelSftp getSharedSftpChannel() throws JSchException {
		if (sharedSftpChannel == null) {
			sharedSftpChannel = openSftpChannel();
		}
		return sharedSftpChannel;
	}

	protected ChannelSftp openSftpChannel() throws JSchException {
		Channel channel = getSharedSession().openChannel("sftp");
		if (logger.isDebugEnabled())
			logger.debug("Opened SFTP channel to " + this);
		channel.connect();
		return (ChannelSftp) channel;
	}

	protected void closeSftpChannel(ChannelSftp sftpChannel) {
		if (sftpChannel != null) {
			sftpChannel.disconnect();
			if (logger.isDebugEnabled())
				logger.debug("Closed SFTP channel to " + this);
		}
	}

	public String toString() {
		return username + "@" + host + ":" + port + " (sftp)";
	}

	private Logger logger = LoggerFactory.getLogger(SshSftpHostConnection.class);

}

