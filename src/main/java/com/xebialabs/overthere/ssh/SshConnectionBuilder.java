package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

@Protocol(name = "ssh")
public class SshConnectionBuilder implements OverthereConnectionBuilder {

	/**
	 * Name of the {@link ConnectionOptions connection option} used to specify the {@link SshConnectionType SSH connection type} to use. Defaults to
	 * {@link SshConnectionType#SFTP}.
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

	private SshConnection connection;

	public SshConnectionBuilder(String type, ConnectionOptions options) {
		SshConnectionType sshConnectionType = options.get(CONNECTION_TYPE, SFTP);
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

}
