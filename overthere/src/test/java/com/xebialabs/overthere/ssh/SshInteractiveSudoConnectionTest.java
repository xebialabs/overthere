package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import org.junit.Before;
import org.junit.Test;

public class SshInteractiveSudoConnectionTest {

	private ConnectionOptions connectionOptions;

	@Before
	public void init() {
		connectionOptions = new ConnectionOptions();
		connectionOptions.set(ConnectionOptions.PASSWORD, "foo");
		connectionOptions.set(ConnectionOptions.OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAcceptPasswordPromptRegexWithWildcardStar() {
		connectionOptions.set(SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX, "assword*");
		new SshInteractiveSudoConnection("ssh", connectionOptions);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAcceptPasswordPromptRegexWithWildcardQuestion() {
		connectionOptions.set(SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX, "assword?");
		new SshInteractiveSudoConnection("ssh", connectionOptions);
	}

	@Test
	public void shouldAcceptPasswordPromptRegex() {
		connectionOptions.set(SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX, "[Pp]assword.*:");
		new SshInteractiveSudoConnection("ssh", connectionOptions);
	}
}
