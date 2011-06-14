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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;

/**
 * An output stream to a file on a host connected through SSH w/ SFTP.
 */
class SshSftpOutputStream extends FilterOutputStream {

	private SshSftpOverthereFile file;

	private ChannelSftp sftpChannel;

	public SshSftpOutputStream(SshSftpOverthereFile file, ChannelSftp sftpChannel, OutputStream out) {
		super(out);
		this.file = file;
		this.sftpChannel = sftpChannel;
	}

	public void close() throws IOException {
		checkState(sftpChannel != null, "Cannot close SFTP output stream that has already been closed");

		super.close();

		((SshSftpOverthereConnection) file.getConnection()).closeSftpChannel(sftpChannel, false);
		sftpChannel = null;

		if(logger.isDebugEnabled())
			logger.debug("Closed SFTP output stream to write to file " + file);
	}

	private static Logger logger = LoggerFactory.getLogger(SshSftpOutputStream.class);

}

