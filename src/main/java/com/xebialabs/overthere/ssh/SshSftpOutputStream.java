package com.xebialabs.overthere.ssh;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.jcraft.jsch.ChannelSftp;

/**
 * An output stream to a file on a host connected through SSH w/ SFTP.
 */
class SshSftpOutputStream extends FilterOutputStream {

	private SshSftpHostConnection session;

	private ChannelSftp sftpChannel;

	public SshSftpOutputStream(SshSftpHostConnection session, ChannelSftp sftpChannel, OutputStream out) {
		super(out);
		this.session = session;
		this.sftpChannel = sftpChannel;
	}

	public void close() throws IOException {
		super.close();
		session.closeSftpChannel(sftpChannel);
	}

}
