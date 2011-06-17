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

import java.io.IOException;

import net.schmizz.sshj.sftp.SFTPClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.Protocol;

/**
 * A connection to a remote host using SSH w/ SFTP.
 */
@Protocol(name = "ssh_sftp")
public class SshSftpOverthereConnection extends SshOverthereConnection {

	private SFTPClient sharedSftpClient;

	public SshSftpOverthereConnection(String type, ConnectionOptions options) {
		super(type, options);
	}

	@Override
	public OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
		return new SshSftpOverthereFile(this, hostPath);
	}

    @Override
    public SshOverthereConnection connect() throws RuntimeIOException {
        SshOverthereConnection connect = super.connect();

        logger.debug("Opening SFTP client to {}", this);
        try {
            sharedSftpClient = getSshClient().newSFTPClient();
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot make SFTP connection to " + this, e);
        }

        return connect;
    }

	@Override
	public void doDisconnect() {
        Preconditions.checkState(sharedSftpClient != null, "Not connected to SFTP");

        logger.debug("Closing SFTP client to {}", this);
        try {
            sharedSftpClient.close();
        } catch (IOException e) {
            logger.error("Couldn't close the SFTP client", e);
        }

        sharedSftpClient = null;
        super.doDisconnect();
	}

    protected SFTPClient getSharedSftpClient() {
		return sharedSftpClient;
	}

    private Logger logger = LoggerFactory.getLogger(SshSftpOverthereConnection.class);

}
