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

import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.ConsoleOverthereProcessOutputHandler.consoleHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.OutputSupplier;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnectionItestBase;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.local.LocalFile;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

public abstract class SshConnectionItestBase extends OverthereConnectionItestBase {

	@Test
	public void commandWithPipeShouldHaveTwoSudoSections() {
		Assume.assumeThat(connection, instanceOf(SshSudoConnection.class));
		List<CmdLineArgument> prepended = ((SshSudoConnection) connection).prefixWithSudoCommand(CmdLine.build("a", "|", "b")).getArguments();
		assertThat(prepended.size(), equalTo(9));
		assertThat(prepended.get(0).toString(false), equalTo("sudo"));
		assertThat(prepended.get(5).toString(false), equalTo("sudo"));
	}

	@Test
	public void commandWithSemiColonShouldHaveTwoSudoSections() {
		Assume.assumeThat(connection, instanceOf(SshSudoConnection.class));
		List<CmdLineArgument> prepended = ((SshSudoConnection) connection).prefixWithSudoCommand(CmdLine.build("a", ";", "b")).getArguments();
		assertThat(prepended.size(), equalTo(9));
		assertThat(prepended.get(0).toString(false), equalTo("sudo"));
		assertThat(prepended.get(5).toString(false), equalTo("sudo"));
	}

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
		assumeThat(options.get("password"), notNullValue());

		options.set("password", "an-incorrect-password");
		try {
			Overthere.getConnection(type, options);
			fail("Expected not to be able to connect with an incorrect password");
		} catch (RuntimeIOException expected) {
		}
	}

	@Test
	public void shouldExecuteSimpleCommand() {
		CapturingOverthereProcessOutputHandler captured = capturingHandler();
		OverthereProcessOutputHandler handler = multiHandler(consoleHandler(), captured);
		int res = connection.execute(handler, CmdLine.build("ls", "-ld", "/tmp"));
		assertThat(res, equalTo(0));
		if (captured.getOutputLines().size() == 2) {
			// When using ssh_interactive_sudo, the first line may contain a password prompt.
			assertThat(captured.getOutputLines().get(0), containsString("assword"));
			assertThat(captured.getOutputLines().get(1), containsString("drwxrwxrwt"));
		} else {
			assertThat(captured.getOutputLines().size(), equalTo(1));
			assertThat(captured.getOutput(), containsString("drwxrwxrwt"));
		}
	}

	@Test
	public void shouldStartProcessSimpleCommand() throws IOException, InterruptedException {
		OverthereProcess p = connection.startProcess(CmdLine.build("ls", "-ld", "/tmp"));
		try {
			String commandOutput = CharStreams.toString(new InputStreamReader(p.getStdout()));
			assertThat(p.waitFor(), equalTo(0));
			assertThat(commandOutput, containsString("drwxrwxrwt"));
		} finally {
			p.destroy();
		}
	}

	@Test
	public void shouldCopyTemporaryFileToOtherLocation() throws IOException {
		OverthereFile tempFile = connection.getTempFile("temporaryFileCanBeWritten.txt");
		OverthereUtils.write("Some test data", "UTF-8", tempFile);

		String dest = "/tmp/" + System.currentTimeMillis() + ".tmp";
		int res = connection.execute(consoleHandler(), CmdLine.build("cp", tempFile.getPath(), dest));
		assertThat(res, equalTo(0));

		connection.execute(consoleHandler(), CmdLine.build("rm", dest));
	}

	/**
	 * Tests whether getOutputStream and getInputStream have the right permission behaviour (specifically for SSH/SUDO connections).
	 */
	@Test
	public void shouldWriteFileToAndReadFileFromSudoUserHomeDirectory() throws IOException {
		// get handle to file in home dir
		final OverthereFile homeDir = connection.getFile(getHomeDirPath());
		final OverthereFile fileInHomeDir = homeDir.getFile("file" + System.currentTimeMillis() + ".dat");
		assertThat(fileInHomeDir.exists(), equalTo(false));

		// write data to file in home dir
		final byte[] contentsWritten = generateRandomBytes(SMALL_FILE_SIZE);
		ByteStreams.write(contentsWritten, new OutputSupplier<OutputStream>() {
			@Override
            public OutputStream getOutput() throws IOException {
	            return fileInHomeDir.getOutputStream();
            }
		});
		
		assertThat(fileInHomeDir.exists(), equalTo(true));

		// restrict access to file in home dir
		connection.execute(consoleHandler(), CmdLine.build("chmod", "0600", fileInHomeDir.getPath()));

		// read file from home dir
		byte[] contentsRead = new byte[SMALL_FILE_SIZE];
		InputStream in = fileInHomeDir.getInputStream();
		try {
			ByteStreams.readFully(in, contentsRead);
		} finally {
			in.close();
		}
		assertThat(contentsRead, equalTo(contentsWritten));

		// restrict access to file in home dir
		fileInHomeDir.delete();
		assertThat(fileInHomeDir.exists(), equalTo(false));
	}

	/**
	 * Tests whether copyTo has the right permission behaviour (specifically for SSH/SUDO connections).
	 */
	@Test
	public void shouldCopyFileToAndFromSudoUserHomeDirectory() throws IOException {
		// get handle to file in home dir
		final OverthereFile homeDir = connection.getFile(getHomeDirPath());
		final OverthereFile fileInHomeDir = homeDir.getFile("file" + System.currentTimeMillis() + ".dat");
		assertThat(fileInHomeDir.exists(), equalTo(false));

		// write random data to local file
		File smallFile = temp.newFile("small.dat");
		byte[] contentsWritten = writeRandomBytes(smallFile, SMALL_FILE_SIZE);
		OverthereFile smallLocalFile = LocalFile.valueOf(smallFile);
		
		// copy local file to file in home dir
		smallLocalFile.copyTo(fileInHomeDir);

		assertThat(fileInHomeDir.exists(), equalTo(true));
		
		// restrict access to file in home dir
		connection.execute(consoleHandler(), CmdLine.build("chmod", "0600", fileInHomeDir.getPath()));

		// copy file in home dir to local file
		File smallFileReadBack = temp.newFile("small-read-back.dat");
		OverthereFile smallLocalFileReadBack = LocalFile.valueOf(smallFileReadBack);
		fileInHomeDir.copyTo(smallLocalFileReadBack);
		
		// read new local file
		byte[] contentsRead = new byte[SMALL_FILE_SIZE];
		InputStream in = smallLocalFileReadBack.getInputStream();
		ByteStreams.readFully(in, contentsRead);

		assertThat(contentsRead, equalTo(contentsWritten));

		// remove file from home dir
		fileInHomeDir.delete();
		assertThat(fileInHomeDir.exists(), equalTo(false));
	}

	protected String getHomeDirPath() {
		String sudoUsername = options.get(SUDO_USERNAME);
		if(sudoUsername != null) {
			return "/home/" + sudoUsername;
		} else {
			return "/home/" + options.get(USERNAME);
		}
    }

}
