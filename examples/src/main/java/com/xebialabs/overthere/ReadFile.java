package com.xebialabs.overthere;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;


public class ReadFile {

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
			BufferedReader r = new BufferedReader(new InputStreamReader(motd.getInputStream()));
			try {
				String line;
				while((line = r.readLine()) != null) {
					System.err.println(line);
				}
			} finally {
				r.close();
			}
		} finally {
			connection.close();
		}
	}

}
