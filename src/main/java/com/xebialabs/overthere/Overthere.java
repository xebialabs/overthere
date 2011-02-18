package com.xebialabs.overthere;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import com.xebialabs.overthere.local.LocalHostConnection;
import com.xebialabs.overthere.ssh.SshInteractiveSudoHostConnection;
import com.xebialabs.overthere.ssh.SshScpHostConnection;
import com.xebialabs.overthere.ssh.SshSftpHostConnection;
import com.xebialabs.overthere.ssh.SshSudoHostConnection;

/**
 * FIXME: Removed functionality:
 * 
 * - untar -> separate utility method, maybe not in here?
 * 
 * - copy resource to temp file -> add helpers to plugin-api
 * 
 * - copy resource to file -> actually only needed by "copy resource to temp file" method
 * 
 * - unreachable host support/tunneled host session -> needs to be reimplemented in a nice way.
 */
public class Overthere {
	/**
	 * The default timeout for opening a connection in milliseconds.
	 */
	// FIXME: should this not be moved somewhere else?
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 120000;

	private static List<Class<? extends HostConnectionBuilder>> builderClasses;

	static {
		builderClasses = newArrayList();
		builderClasses.add(LocalHostConnection.class);
	}

	public static HostConnection getConnection(String type, ConnectionOptions options) {
		// FIXME: should be way more generic, invoking classes that were scanned from the classpath
		if (type.equals("local")) {
			return new LocalHostConnection(type, options).connect();
		} else if (type.equals("ssh_sftp")) {
			return new SshSftpHostConnection(type, options).connect();
		} else if (type.equals("ssh_scp")) {
			return new SshScpHostConnection(type, options).connect();
		} else if (type.equals("ssh_sudo")) {
			return new SshSudoHostConnection(type, options).connect();
		} else if (type.equals("ssh_interactive_sudo")) {
			return new SshInteractiveSudoHostConnection(type, options).connect();
		} else {
			throw new IllegalArgumentException("Unknown connection type " + type);
		}
	}

}
