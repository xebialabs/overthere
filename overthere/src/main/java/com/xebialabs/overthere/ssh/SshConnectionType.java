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
	 * An SSH connection that uses SFTP to transfer files, to a Windows host running WinSSHD.
	 */
	SFTP_WINSSHD,

	/**
	 * An SSH connection that uses SCP to transfer files, to a Unix host.
	 */
	SCP,

	/**
	 * An SSH connection that uses SCP to transfer files, to a Unix host. Uses SUDO, configured with NOPASSWD for all commands, to execute commands.
	 */
	SUDO,

	/**
	 * An SSH connection that uses SCP to transfer files, to a Unix host. Uses SUDO, <em>not</em> been configured with NOPASSWD for all commands, to execute commands.
	 */
	INTERACTIVE_SUDO,

	/**
	 * An SSH connection that is used for tunneling another connection through a 'jump station'. No operation on this actual connection can be performed.
	 */
	TUNNEL
}
