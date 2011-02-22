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

