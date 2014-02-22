package com.xebialabs.overthere;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;

public class ExecuteOnUnix {

	public static void main(String[] args) {
		ConnectionOptions options = new ConnectionOptions();
		options.set(ADDRESS, "unix-box");
		options.set(USERNAME, "demo");
		options.set(PASSWORD, "secret");
		options.set(OPERATING_SYSTEM, UNIX);
		options.set(CONNECTION_TYPE, SFTP);
		OverthereConnection connection = Overthere.getConnection("ssh", options);
		try {
			connection.execute(CmdLine.build("cat", "/etc/motd"));
		} finally {
			connection.close();
		}
	}

}
