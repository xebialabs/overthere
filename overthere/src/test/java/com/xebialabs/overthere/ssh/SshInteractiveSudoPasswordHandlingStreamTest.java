/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX_DEFAULT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;

public class SshInteractiveSudoPasswordHandlingStreamTest {

	private OutputStream os;

	@Before
	public void init() {
		os = mock(OutputStream.class);
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

