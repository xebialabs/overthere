package com.xebialabs.overthere;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

import java.io.IOException;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;

public class GetFileInfo {

	public static void main(String[] args) throws IOException {
		ConnectionOptions options = new ConnectionOptions();
		options.set(ADDRESS, "unix-box");
		options.set(USERNAME, "demo");
		options.set(PASSWORD, "secret");
		options.set(OPERATING_SYSTEM, UNIX);
		options.set(CONNECTION_TYPE, SFTP);
		OverthereConnection connection = Overthere.getConnection("ssh", options);
		try {
			OverthereFile motd = connection.getFile("/etc/motd");
			System.out.println("Length        : " + motd.length());
			System.out.println("Last modified : " + motd.lastModified());
			System.out.println("Exists        : " + motd.exists());
			System.out.println("Can read      : " + motd.canRead());
			System.out.println("Can write     : " + motd.canWrite());
			System.out.println("Can execute   : " + motd.canExecute());
		} finally {
			connection.close();
		}
	}

}
