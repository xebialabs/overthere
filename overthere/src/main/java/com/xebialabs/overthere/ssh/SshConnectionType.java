package com.xebialabs.overthere.ssh;

/**
 * Enumeration of SSH connection types.
 */
public enum SshConnectionType {

	/**
	 * An SSH connection that uses SFTP to transfer files, to a Unix host.
	 */
	SFTP,

	/**
	 * An SSH connection that uses SFTP to transfer files, to a Windows host running OpenSSH on Cygwin.
	 */
	SFTP_CYGWIN,

	/**
	 * An SSH connection that uses SCP to transfer files, to a Unix host.
	 */
	SCP,

	/**
	 * An SSH connection that uses SCP to transfer files and SUDO to execute commands. SUDO has been configured with NOPASSWD for all commands, to a Unix host..
	 */
	SUDO,

	/**
	 * An SSH connection that uses SCP to transfer files and SUDO to execute commands. SUDO has <em>not</em> been configured with NOPASSWD for all commands, to a Unix host..
	 */
	INTERACTIVE_SUDO

}
