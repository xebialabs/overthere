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

import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.CommandExecution;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.DebugCommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostSessionItestBase;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

public abstract class SshHostConnectionItestBase extends HostSessionItestBase {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Test
	public void cannotConnectWithIncorrectUsername() {
		options.set("username", "an-incorrect-username");
		try {
			Overthere.getConnection(type, options);
			fail("Expected not to be able to connect with an incorrect username");
		} catch (RuntimeIOException expected) {
		}
	}

	@Test
	public void cannotConnectWithIncorrectPassword() {
		options.set("password", "an-incorrect-password");
		try {
			Overthere.getConnection(type, options);
			fail("Expected not to be able to connect with an incorrect password");
		} catch (RuntimeIOException expected) {
		}
	}

	@Test
	public void executeSimpleCommand() {
		CapturingCommandExecutionCallbackHandler handler = new CapturingCommandExecutionCallbackHandler();
		int res = connection.execute(handler, "ls", "-ld", "/tmp");
		assertThat(res, equalTo(0));
		assertThat(handler.getOutputLines().size(), equalTo(1));
		assertThat(handler.getOutput(), containsString("drwxrwxrwt"));
	}

	@Test
	public void startExecuteSimpleCommand() throws IOException {
		CommandExecution execution = connection.startExecute("ls", "-ld", "/tmp");
		String commandOutput = IOUtils.toString(execution.getStdout());
		assertThat(execution.waitFor(), equalTo(0));
		assertThat(commandOutput, containsString("drwxrwxrwt"));
	}

	@Test
	public void temporaryFileCanBeWritten() throws Exception {
		final File tempFile = temp.newFile("fooBar");
		writeStringToFile(tempFile, "Foo Bar Baz");

		OverthereFile file = connection.copyToTemporaryFile(tempFile);

		assertThat("Expected temp file to have been written", file.exists(), equalTo(true));
	}

	@Test
	public void temporaryFileCanBeCopiedToOtherLocation() throws IOException {
		final File tempFile = temp.newFile("fooBar");
		writeStringToFile(tempFile, "Foo Bar Baz");

		OverthereFile file = connection.copyToTemporaryFile(tempFile);

		CommandExecutionCallbackHandler handler = new DebugCommandExecutionCallbackHandler();
		int res = connection.execute(handler, "cp", file.getPath(), "/tmp/" + System.currentTimeMillis() + ".class");
		assertThat(res, equalTo(0));
	}

}
