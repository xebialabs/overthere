package com.xebialabs.overthere.ssh;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.jcraft.jsch.ChannelSftp;

/**
 * An input stream from a file on a host connected through SSH w/ SFTP.
 */
class SshSftpInputStream extends FilterInputStream {

	private SshSftpHostConnection session;

	private ChannelSftp sftpChannel;

	public SshSftpInputStream(SshSftpHostConnection session, ChannelSftp sftpChannel, InputStream in) {
		super(in);
		this.session = session;
		this.sftpChannel = sftpChannel;
	}

	public void close() throws IOException {
		super.close();
		session.closeSftpChannel(sftpChannel);
	}

}
