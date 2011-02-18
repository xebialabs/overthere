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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ClassUtils;

import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.CommandExecution;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.DebugCommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;

public abstract class SshSudoHostSessionItestBase extends SshHostSessionItestBase {

	@Test
	public void executeCommandWithSudoThatAsksForPassword() {
		CapturingCommandExecutionCallbackHandler handler = new CapturingCommandExecutionCallbackHandler();
		int res = session.execute(handler, "ls", "-ld", "/tmp");
		assertEquals(0, res);
		assertEquals(1, handler.getOutputLines().size());
		assertTrue(handler.getOutput().contains("drwxrwxrwt"));
	}

	@Test
	public void startExecuteCommandWithSudoThatAsksForPassword() throws IOException {
		CommandExecution execution = session.startExecute("ls", "-ld", "/tmp");
		String commandOutput = IOUtils.toString(execution.getStdout());
		assertEquals(0, execution.waitFor());
		assertTrue(commandOutput.contains("drwxrwxrwt"));
	}

	@Test
	public void tempDirectoryIsWritableForScpUser() throws Exception {
		final String classAsResource = ClassUtils.convertClassNameToResourcePath(getClass().getName()) + ClassUtils.CLASS_FILE_SUFFIX;
		HostFile file = session.copyToTemporaryFile(new ClassPathResource(classAsResource));
		assertTrue("Expected temp file to have been written", file.exists());
	}

	@Test
	public void writtenFileIsReadableForSudoUser() {
		final String classAsResource = ClassUtils.convertClassNameToResourcePath(getClass().getName()) + ClassUtils.CLASS_FILE_SUFFIX;
		HostFile file = session.copyToTemporaryFile(new ClassPathResource(classAsResource));

		CommandExecutionCallbackHandler handler = new DebugCommandExecutionCallbackHandler();
		int res = session.execute(handler, "cp", file.getPath(), "/tmp/" + System.currentTimeMillis() + ".class");
		assertEquals(0, res);
	}

	@Test
	public void commandWithPipeShouldHaveTwoSudoSections() {
		String[] prepended = ((SshSudoHostSession) session).prependSudoCommand("a", "|", "b");
		assertEquals(9, prepended.length);
		assertEquals("sudo", prepended[0]);
		assertEquals("sudo", prepended[5]);
	}

	@Test
	public void commandWithSemiColonShouldHaveTwoSudoSections() {
		String[] prepended = ((SshSudoHostSession) session).prependSudoCommand("a", ";", "b");
		assertEquals(9, prepended.length);
		assertEquals("sudo", prepended[0]);
		assertEquals("sudo", prepended[5]);
	}

}
