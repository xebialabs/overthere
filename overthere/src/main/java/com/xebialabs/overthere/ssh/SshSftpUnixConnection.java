package com.xebialabs.overthere.ssh;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;

import com.xebialabs.overthere.ConnectionOptions;

/**
 * A connection to a remote Unix host using SSH w/ SFTP.
 */
class SshSftpUnixConnection extends SshSftpConnection {

	public SshSftpUnixConnection(String type, ConnectionOptions options) {
	    super(type, options);
		checkArgument(os != WINDOWS, "Cannot start a " + SSH_PROTOCOL + ":%s connection to a Windows operating system", sshConnectionType.toString().toLowerCase());
    }

	@Override
	protected String pathToSftpPath(String path) {
	    return path;
    }

}
