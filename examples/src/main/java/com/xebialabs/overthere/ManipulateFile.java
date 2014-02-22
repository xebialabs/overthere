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

public class ManipulateFile {

	public static void main(String[] args) throws IOException {
		ConnectionOptions options = new ConnectionOptions();
		options.set(ADDRESS, "unix-box");
		options.set(USERNAME, "demo");
		options.set(PASSWORD, "secret");
		options.set(OPERATING_SYSTEM, UNIX);
		options.set(CONNECTION_TYPE, SFTP);
		OverthereConnection connection = Overthere.getConnection("ssh", options);
		try {
			connection.execute(CmdLine.build("cp", "/etc/motd", "/tmp/motd1"));
			OverthereFile motd1 = connection.getFile("/tmp/motd1");
			OverthereFile motd2 = connection.getFile("/tmp/motd2");
			motd2.delete();

			System.err.println("Exists #1: " + motd1.exists());
			System.err.println("Exists #2: " + motd2.exists());

			motd1.renameTo(motd2);
			System.err.println("Exists #1: " + motd1.exists());
			System.err.println("Exists #2: " + motd2.exists());

			motd2.copyTo(motd1);
			System.err.println("Exists #1: " + motd1.exists());
			System.err.println("Exists #2: " + motd2.exists());

			motd1.delete();
			motd2.delete();
			System.err.println("Exists #1: " + motd1.exists());
			System.err.println("Exists #2: " + motd2.exists());
		} finally {
			connection.close();
		}
	}

}
