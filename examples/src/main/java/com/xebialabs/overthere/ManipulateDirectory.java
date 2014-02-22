package com.xebialabs.overthere;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

import java.io.IOException;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;

public class ManipulateDirectory {

	public static void main(String[] args) throws IOException {
		ConnectionOptions options = new ConnectionOptions();
		options.set(ADDRESS, "unix-box");
		options.set(USERNAME, "demo");
		options.set(PASSWORD, "secret");
		options.set(OPERATING_SYSTEM, UNIX);
		options.set(CONNECTION_TYPE, SFTP);
		OverthereConnection connection = Overthere.getConnection("ssh", options);
		try {
			connection.execute(CmdLine.build("cp", "-r", "/var/log/apt", "/tmp/logs1"));
			OverthereFile logs1 = connection.getFile("/tmp/logs1");
			OverthereFile logs2 = connection.getFile("/tmp/logs2");
			logs2.delete();

			System.err.println("Exists #1: " + logs1.exists());
			System.err.println("Exists #2: " + logs2.exists());

			logs1.renameTo(logs2);
			System.err.println("Exists #1: " + logs1.exists());
			System.err.println("Exists #2: " + logs2.exists());

			logs2.copyTo(logs1);
			System.err.println("Exists #1: " + logs1.exists());
			System.err.println("Exists #2: " + logs2.exists());

			logs1.deleteRecursively();
			logs2.deleteRecursively();
			System.err.println("Exists #1: " + logs1.exists());
			System.err.println("Exists #2: " + logs2.exists());
		} finally {
			connection.close();
		}
	}

}
