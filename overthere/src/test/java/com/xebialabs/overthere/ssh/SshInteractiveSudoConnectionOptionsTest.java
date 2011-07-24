package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;

import org.junit.Before;
import org.junit.Test;

import com.xebialabs.overthere.ConnectionOptions;

public class SshInteractiveSudoConnectionOptionsTest {

	private ConnectionOptions connectionOptions;

	@Before
	public void init() {
		connectionOptions = new ConnectionOptions();
		connectionOptions.set(OPERATING_SYSTEM, UNIX);
		connectionOptions.set(PASSWORD, "foo");
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
