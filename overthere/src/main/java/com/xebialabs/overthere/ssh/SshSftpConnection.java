/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
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

	public SshSftpConnection(String type, ConnectionOptions options) {
		super(type, options);
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

