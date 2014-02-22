package com.xebialabs.overthere;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;

public class WriteFile {

	public static void main(String[] args) throws IOException {
		ConnectionOptions options = new ConnectionOptions();
		options.set(ADDRESS, "unix-box");
		options.set(USERNAME, "demo");
		options.set(PASSWORD, "secret");
		options.set(OPERATING_SYSTEM, UNIX);
		options.set(CONNECTION_TYPE, SCP);
		OverthereConnection connection = Overthere.getConnection("ssh", options);
		try {
			OverthereFile motd = connection.getFile("/tmp/new-motd");
			PrintWriter w = new PrintWriter(motd.getOutputStream());
			try {
				w.println("An Overthere a day keeps the doctor away");
				w.println("Written on " + new java.util.Date() + " from " + InetAddress.getLocalHost() + " running " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
			} finally {
				w.close();
			}
			connection.execute(CmdLine.build("cat", "/tmp/new-motd"));
		} finally {
			connection.close();
		}
	}

}
