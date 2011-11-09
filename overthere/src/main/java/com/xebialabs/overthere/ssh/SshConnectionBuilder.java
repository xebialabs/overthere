package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

/**
 * Builds SSH connections.
 */
@Protocol(name = SSH_PROTOCOL)
public class SshConnectionBuilder implements OverthereConnectionBuilder {

	/**
	 * Name of the protocol handled by this connection builder, i.e. "ssh".
	 */
	public static final String SSH_PROTOCOL = "ssh";
	
	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify the {@link SshConnectionType SSH connection type} to use.
	 */
	public static final String CONNECTION_TYPE = "connectionType";

	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify the private key file to use. <b>N.B.:</b> Private keys cannot be used when the
	 * SSH connection type is {@link SshConnectionType#INTERACTIVE_SUDO INTERACTIVE_SUDO} because the password is needed for the password prompts.
	 */
	public static final String PRIVATE_KEY_FILE = "privateKeyFile";

	/**
	 * Name of the {@link ConnectionOptions connection option} use to specify the passphrase of the private key.
	 */
	public static final String PASSPHRASE = "passphrase";

	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify the username to sudo to for {@link SshConnectionType#SUDO SUDO} and
	 * {@link SshConnectionType#INTERACTIVE_SUDO INTERACTIVE_SUDO} SSH connections.
	 */	
	public static final String SUDO_USERNAME = "sudoUsername";

	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify whether a default pty should be allocated when executing a command. All sudo
	 * implementations require it for interactive sudo, some even require it for normal sudo. Some SSH server implementations (notably the one on AIX 5.3) crash
	 * when it is allocated. Defaults to true.
	 */
	public static final String ALLOCATE_DEFAULT_PTY = "allocateDefaultPty";

	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify which expression to look for in interactive sudo before sending the password.
	 */
	public static final String SUDO_PASSWORD_PROMPT_REGEX = "sudoPasswordPromptRegex";
	
	/**
	 * Default value of the {@link ConnectionOptions connection option} used to specify which expression to look for in interactive sudo before sending the
	 * password.
	 */
	public static final String SUDO_PASSWORD_PROMPT_REGEX_DEFAULT = ".*[Pp]assword.*:";

	private SshConnection connection;


	public SshConnectionBuilder(String type, ConnectionOptions options) {
		SshConnectionType sshConnectionType = options.get(CONNECTION_TYPE);

		switch (sshConnectionType) {
		case SFTP:
			connection = new SshSftpConnection(type, options);
			break;
		case SCP:
			connection = new SshScpConnection(type, options);
			break;
		case SUDO:
			connection = new SshSudoConnection(type, options);
			break;
		case INTERACTIVE_SUDO:
			connection = new SshInteractiveSudoConnection(type, options);
			break;
		default:
			throw new IllegalArgumentException("Unknown SSH connection type " + sshConnectionType);
		}
	}

	@Override
	public OverthereConnection connect() {
		connection.connect();
		return connection;
	}

	public String toString() {
		return connection.toString();
	}

}
