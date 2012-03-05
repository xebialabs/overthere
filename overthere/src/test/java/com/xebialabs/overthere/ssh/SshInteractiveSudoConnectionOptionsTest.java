package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;

public class SshInteractiveSudoConnectionOptionsTest {

	private ConnectionOptions connectionOptions;

	@BeforeClass
	public void init() {
		connectionOptions = new ConnectionOptions();
		connectionOptions.set(CONNECTION_TYPE, SFTP);
		connectionOptions.set(OPERATING_SYSTEM, UNIX);
		connectionOptions.set(ADDRESS, "nowhere.example.com");
		connectionOptions.set(USERNAME, "some-user");
		connectionOptions.set(PASSWORD, "foo");
		connectionOptions.set(SUDO_USERNAME, "some-other-user");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldNotAcceptPasswordPromptRegexWithWildcardStar() {
		connectionOptions.set(SUDO_PASSWORD_PROMPT_REGEX, "assword*");
		new SshInteractiveSudoConnection(SSH_PROTOCOL, connectionOptions, new DefaultAddressPortMapper());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void shouldNotAcceptPasswordPromptRegexWithWildcardQuestion() {
		connectionOptions.set(SUDO_PASSWORD_PROMPT_REGEX, "assword?");
		new SshInteractiveSudoConnection(SSH_PROTOCOL, connectionOptions, new DefaultAddressPortMapper());
	}

	@Test
	public void shouldAcceptPasswordPromptRegex() {
		connectionOptions.set(SUDO_PASSWORD_PROMPT_REGEX, "[Pp]assword.*:");
		new SshInteractiveSudoConnection(SSH_PROTOCOL, connectionOptions, new DefaultAddressPortMapper());
	}
}
