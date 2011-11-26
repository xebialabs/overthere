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

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereProcess;

public class StartProcess {

	public static void main(String[] args) throws InterruptedException, IOException {
		ConnectionOptions options = new ConnectionOptions();
		options.set(ADDRESS, "unix-box");
		options.set(USERNAME, "demo");
		options.set(PASSWORD, "secret");
		options.set(OPERATING_SYSTEM, UNIX);
		options.set(CONNECTION_TYPE, SFTP);

		OverthereConnection connection = Overthere.getConnection("ssh", options);
		try {
			OverthereProcess process = connection.startProcess(CmdLine.build("cat", "/etc/motd"));
			BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getStdout()));
			try { 
				String line;
				while((line = stdout.readLine()) != null) {
					System.err.println(line);
				}
			} finally {
				stdout.close();
			}
			int exitCode = process.waitFor();
			System.err.println("Exit code: " + exitCode);
		} finally {
			connection.close();
		}
	}

}
