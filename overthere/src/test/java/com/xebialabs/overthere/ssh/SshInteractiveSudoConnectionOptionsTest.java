package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

import org.junit.Before;
import org.junit.Test;

import com.xebialabs.overthere.ConnectionOptions;

public class SshInteractiveSudoConnectionOptionsTest {

	private ConnectionOptions connectionOptions;

	@Before
	public void init() {
		connectionOptions = new ConnectionOptions();
		connectionOptions.set(CONNECTION_TYPE, SFTP);
		connectionOptions.set(OPERATING_SYSTEM, UNIX);
		connectionOptions.set(ADDRESS, "nowhere.example.com");
		connectionOptions.set(USERNAME, "some-user");
		connectionOptions.set(PASSWORD, "foo");
		connectionOptions.set(SUDO_USERNAME, "some-other-user");
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAcceptPasswordPromptRegexWithWildcardStar() {
		connectionOptions.set(SUDO_PASSWORD_PROMPT_REGEX, "assword*");
		new SshInteractiveSudoConnection(SSH_PROTOCOL, connectionOptions);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAcceptPasswordPromptRegexWithWildcardQuestion() {
		connectionOptions.set(SUDO_PASSWORD_PROMPT_REGEX, "assword?");
		new SshInteractiveSudoConnection(SSH_PROTOCOL, connectionOptions);
	}

	@Test
	public void shouldAcceptPasswordPromptRegex() {
		connectionOptions.set(SUDO_PASSWORD_PROMPT_REGEX, "[Pp]assword.*:");
		new SshInteractiveSudoConnection(SSH_PROTOCOL, connectionOptions);
	}
}
