/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;
import net.schmizz.sshj.sftp.SFTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkState;

/**
 * Base class for connections to a remote host using SSH w/ SFTP.
 */
abstract class SshSftpConnection extends SshConnection {

    private SFTPClient sharedSftpClient;

    public SshSftpConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper);
    }

    @Override
    protected void connect() {
        super.connect();

        logger.debug("Opening SFTP client to {}", this);
        try {
            sharedSftpClient = getSshClient().newSFTPClient();
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot make SFTP connection to " + this, e);
        }
    }

    @Override
    public void doClose() {
        checkState(sharedSftpClient != null, "Not connected to SFTP");

        logger.debug("Closing SFTP client to {}", this);
        try {
            sharedSftpClient.close();
        } catch (IOException e) {
            logger.error("Couldn't close the SFTP client", e);
        }

        sharedSftpClient = null;
        super.doClose();
    }

    protected SFTPClient getSharedSftpClient() {
        return sharedSftpClient;
    }

    @Override
    public OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
        return new SshSftpFile(this, hostPath);
    }

    protected abstract String pathToSftpPath(String path);

    private Logger logger = LoggerFactory.getLogger(SshSftpConnection.class);

}
