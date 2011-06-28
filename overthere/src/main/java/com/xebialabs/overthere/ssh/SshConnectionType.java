package com.xebialabs.overthere.ssh;

/**
 * Enumeration of SSH connection types.
 */
public enum SshConnectionType {

	/**
	 * An SSH connection using SFTP to transfer files.
	 */
	SFTP,

	/**
	 * An SSH connection using SCP to transfer files.
	 */
	SCP,

	/**
	 * An SSH connection using SCP to transfer files and SUDO to execute commands. SUDO has been configured with NOPASSWD for all commands.
	 */
	SUDO,

	/**
	 * An SSH connection using SCP to transfer files and SUDO to execute commands. SUDO has <em>not</em> been configured with NOPASSWD for all commands.
	 */
	INTERACTIVE_SUDO

}
