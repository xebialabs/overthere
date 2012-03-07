package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX_DEFAULT;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SshInteractiveSudoPasswordHandlingStreamTest {

	@Mock
	private OutputStream os;

	@BeforeClass
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldSendPasswordOnMatchingOutput() throws IOException {
		InputStream is = new ByteArrayInputStream("[sudo] password for user bar:".getBytes());
		SshInteractiveSudoPasswordHandlingStream foo = new SshInteractiveSudoPasswordHandlingStream(is, os, "foo", SUDO_PASSWORD_PROMPT_REGEX_DEFAULT);
		readStream(foo);
		verify(os).write("foo\r\n".getBytes());
		verify(os).flush();
	}

	@Test
	public void shouldNotSendPasswordWhenRegexDoesntMatch() throws IOException {
		InputStream is = new ByteArrayInputStream("Password:".getBytes());
		SshInteractiveSudoPasswordHandlingStream foo = new SshInteractiveSudoPasswordHandlingStream(is, os, "foo", ".*[Pp]assword.*>");
		readStream(foo);
		verifyZeroInteractions(os);
	}
	
	private void readStream(SshInteractiveSudoPasswordHandlingStream foo) throws IOException {
		while(foo.available() > 0) {
			foo.read();
		}
	}

}
