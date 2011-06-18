package com.xebialabs.overthere.ssh;

import java.io.IOException;

import net.schmizz.sshj.SSHClient;

import org.junit.Ignore;
import org.junit.Test;

public class SshjConnectionItest {

	private static final String HOSTNAME = "overthere";
	private static final String USERNAME = "overthere";
	private static final String PASSWORD = "overhere";

	@Test
	@Ignore
	public void connectWithSshjAgainAndAgain() throws IOException {
		for (int i = 0;; i++) {
			System.out.println(i);
			SSHClient client = new SSHClient();
			client.setConnectTimeout(120000);
			client.addHostKeyVerifier(new LaxKeyVerifier());
			client.connect(HOSTNAME, 22);
			client.authPassword(USERNAME, PASSWORD);
			client.disconnect();
		}
	}
	
}
