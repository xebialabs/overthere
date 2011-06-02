/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereConnectionItestBase;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;
import com.xebialabs.overthere.util.ConsoleOverthereProcessOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

public abstract class SshOverthereConnectionItestBase extends OverthereConnectionItestBase {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void shouldNotConnectWithIncorrectUsername() {
		options.set("username", "an-incorrect-username");
		try {
			Overthere.getConnection(type, options);
			fail("Expected not to be able to connect with an incorrect username");
		} catch (RuntimeIOException expected) {
		}
	}

	@Test
	public void shouldNotConnectWithIncorrectPassword() {
		options.set("password", "an-incorrect-password");
		try {
			Overthere.getConnection(type, options);
			fail("Expected not to be able to connect with an incorrect password");
		} catch (RuntimeIOException expected) {
		}
	}

	@Test
	public void shouldExecuteSimpleCommand() {
		CapturingOverthereProcessOutputHandler handler = capturingHandler();
		int res = connection.execute(handler, CmdLine.build("ls", "-ld", "/tmp"));
		assertThat(res, equalTo(0));
		if (handler.getOutputLines().size() == 2) {
			// When using ssh_interactive_sudo, the first line may contain a password prompt.
			assertThat(handler.getOutputLines().get(0), containsString("assword"));
			assertThat(handler.getOutputLines().get(1), containsString("drwxrwxrwt"));
		} else {
			assertThat(handler.getOutputLines().size(), equalTo(1));
			assertThat(handler.getOutput(), containsString("drwxrwxrwt"));
		}
	}

	@Test
	public void shouldStartProcessSimpleCommand() throws IOException, InterruptedException {
		OverthereProcess p = connection.startProcess(CmdLine.build("ls", "-ld", "/tmp"));
		String commandOutput = IOUtils.toString(p.getStdout());
		assertThat(p.waitFor(), equalTo(0));
		assertThat(commandOutput, containsString("drwxrwxrwt"));
	}

	@Test
	public void shouldBeAbleToWriteToTemporaryFile() throws Exception {
		OverthereFile tempFile = connection.getTempFile("temporaryFileCanBeWritten.txt");
		OverthereUtils.write("Some test data", "UTF-8", tempFile);

		assertThat("Expected temp file to have been written", tempFile.exists(), equalTo(true));
	}

	@Test
	public void shouldBeAbleToCopyTemporaryFileToOtherLocation() throws IOException {
		OverthereFile tempFile = connection.getTempFile("temporaryFileCanBeWritten.txt");
		OverthereUtils.write("Some test data", "UTF-8", tempFile);

		OverthereProcessOutputHandler handler = new ConsoleOverthereProcessOutputHandler();
		int res = connection.execute(handler, CmdLine.build("cp", tempFile.getPath(), "/tmp/" + System.currentTimeMillis() + ".class"));
		assertThat(res, equalTo(0));
	}

}
