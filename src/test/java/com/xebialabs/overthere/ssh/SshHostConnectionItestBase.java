/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.overthere.ssh;

import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.HostSessionItestBase;
import com.xebialabs.overthere.Overthere;
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
		assertEquals(0, res);
		assertEquals(1, handler.getOutputLines().size());
		assertTrue(handler.getOutput().contains("drwxrwxrwt"));
	}

	@Test
	public void startExecuteSimpleCommand() throws IOException {
		CommandExecution execution = connection.startExecute("ls", "-ld", "/tmp");
		String commandOutput = IOUtils.toString(execution.getStdout());
		assertEquals(0, execution.waitFor());
		assertTrue(commandOutput.contains("drwxrwxrwt"));
	}

	@Test
	public void temporaryFileCanBeWritten() throws Exception {
		final File tempFile = temp.newFile("fooBar");
		writeStringToFile(tempFile, "Foo Bar Baz");

		HostFile file = connection.copyToTemporaryFile(tempFile);

		assertTrue("Expected temp file to have been written", file.exists());
	}

	@Test
	public void temporaryFileCanBeCopiedToOtherLocation() throws IOException {
		final File tempFile = temp.newFile("fooBar");
		writeStringToFile(tempFile, "Foo Bar Baz");

		HostFile file = connection.copyToTemporaryFile(tempFile);

		CommandExecutionCallbackHandler handler = new DebugCommandExecutionCallbackHandler();
		int res = connection.execute(handler, "cp", file.getPath(), "/tmp/" + System.currentTimeMillis() + ".class");
		assertEquals(0, res);
	}

}
