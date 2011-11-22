package com.xebialabs.overthere.ssh;

/**
 * Enumeration of SSH connection types.
 */
public enum SshConnectionType {

	/**
	 * An SSH connection that uses SFTP to transfer files.
	 */
	SFTP,

	/**
	 * An SSH connection that uses SFTP to transfer files, to an OpenSSH server in Cygwin on Windows.
	 */
	SFTP_CYGWIN,

	/**
	 * An SSH connection that uses SCP to transfer files.
	 */
	SCP,

	/**
	 * An SSH connection that uses SCP to transfer files and SUDO to execute commands. SUDO has been configured with NOPASSWD for all commands.
	 */
	SUDO,

	/**
	 * An SSH connection that uses SCP to transfer files and SUDO to execute commands. SUDO has <em>not</em> been configured with NOPASSWD for all commands.
	 */
	INTERACTIVE_SUDO

}
