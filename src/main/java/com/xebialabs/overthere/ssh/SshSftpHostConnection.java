package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.RuntimeIOException;
import org.slf4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.xebialabs.overthere.HostFile;
import org.slf4j.LoggerFactory;

/**
 * A connection to a remote host using SSH w/ SFTP.
 */
public class SshSftpHostConnection extends SshHostConnection {

	private ChannelSftp sharedSftpChannel;

	public SshSftpHostConnection(String type, ConnectionOptions options) {
		super(type, options);
	}

	@Override
	public HostFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
		return new SshSftpHostFile(this, hostPath);
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
