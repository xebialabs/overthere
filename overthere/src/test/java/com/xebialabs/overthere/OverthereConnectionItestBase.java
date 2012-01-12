/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
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

package com.xebialabs.overthere;

import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.ConsoleOverthereProcessOutputHandler.consoleHandler;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import com.xebialabs.overthere.local.LocalFile;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

/**
 * Base class for all Overthere connection itests.
 */
public abstract class OverthereConnectionItestBase {

	private static final int NR_OF_SMALL_FILES = 100;

	public static final int SMALL_FILE_SIZE = 10 * 1024;

	public static final int LARGE_FILE_SIZE = 1 * 1024 * 1024;

	protected String type;

	protected ConnectionOptions options;

	protected String expectedConnectionClassName;

	protected OverthereConnection connection;

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();
	
	@Before
	public void connect() throws Exception {
		setTypeAndOptions();
		connection = Overthere.getConnection(type, options);
	}

	protected abstract void setTypeAndOptions() throws Exception;

	@After
	public void disconnect() {
		if (connection != null) {
			try {
				connection.close();
				connection = null;
			} catch(Exception exc) {
				System.out.println("Exception while disconnecting at end of test case:");
				exc.printStackTrace(System.out);
			}
		}
	}

	@Test
	public void connectionObjectShouldBeInstanceOfExpectedClass() {
		assertThat(connection.getClass().getName(), equalTo(expectedConnectionClassName));
	}

	@SuppressWarnings("unchecked")
    @Test
	public void shouldNotConnectWithIncorrectUsername() {
		assumeThat(type, allOf(not(equalTo(LOCAL_PROTOCOL)), not(equalTo(CIFS_PROTOCOL))));

		options.set("username", "an-incorrect-username");
		try {
			Overthere.getConnection(type, options);
			fail("Expected not to be able to connect with an incorrect username");
		} catch (RuntimeIOException expected) {
		}
	}

	@SuppressWarnings("unchecked")
    @Test
	public void shouldNotConnectWithIncorrectPassword() {
		assumeThat(type, allOf(not(equalTo(LOCAL_PROTOCOL)), not(equalTo(CIFS_PROTOCOL))));
		assumeThat(options.getOptional("password"), notNullValue());

		options.set("password", "an-incorrect-password");
		try {
			Overthere.getConnection(type, options);
			fail("Expected not to be able to connect with an incorrect password");
		} catch (RuntimeIOException expected) {
		}
	}

	@Test
	public void shouldExecuteSimpleCommandOnUnix() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(UNIX));

		CapturingOverthereProcessOutputHandler captured = capturingHandler();
		int res = connection.execute(multiHandler(consoleHandler(), captured), CmdLine.build("ls", "-ld", "/tmp/."));
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
	public void shouldExecuteSimpleCommandInWorkingDirectoryOnUnix() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(UNIX));

		connection.setWorkingDirectory(connection.getFile("/etc"));
		CapturingOverthereProcessOutputHandler captured = capturingHandler();
		int res = connection.execute(multiHandler(consoleHandler(), captured), CmdLine.build("pwd"));
		assertThat(res, equalTo(0));
		assertThat(captured.getOutput(), containsString("/etc"));
	}

	@Test
	public void shouldCaptureLastLineOfSimpleCommandOnUnix() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(UNIX));

		CapturingOverthereProcessOutputHandler captured = capturingHandler();
		int res = connection.execute(multiHandler(consoleHandler(), captured), CmdLine.build("echo", "-n", "line", "that", "does", "not", "end", "in", "a", "newline"));
		assertThat(res, equalTo(0));
		if (captured.getOutputLines().size() == 2) {
			// When using ssh_interactive_sudo, the first line may contain a password prompt.
			assertThat(captured.getOutputLines().get(0), containsString("assword"));
			assertThat(captured.getOutputLines().get(1), containsString("line that does not end in a newline"));
		} else {
			assertThat(captured.getOutputLines().size(), equalTo(1));
			assertThat(captured.getOutput(), containsString("line that does not end in a newline"));
		}
	}

	@Test
	public void shouldStartProcessSimpleCommandOnUnix() throws IOException, InterruptedException {
		assumeThat(connection.getHostOperatingSystem(), equalTo(UNIX));

		OverthereProcess p = connection.startProcess(CmdLine.build("ls", "-ld", "/tmp/."));
		try {
			String commandOutput = CharStreams.toString(new InputStreamReader(p.getStdout()));
			assertThat(p.waitFor(), equalTo(0));
			assertThat(commandOutput, containsString("drwxrwxrwt"));
		} finally {
			p.destroy();
		}
	}

	@Test
	public void shouldListFilesOnWindows() throws IOException {
		assumeThat(connection.getHostOperatingSystem(), equalTo(WINDOWS));

		OverthereFile folder = connection.getFile("C:\\overthere");
		List<OverthereFile> filesInFolder = folder.listFiles();

		OverthereFile expectedFile = connection.getFile("C:\\overthere\\overhere.txt");
		assertThat(filesInFolder.contains(expectedFile), equalTo(true));
	}

	@Test
	public void shouldExecuteSimpleCommandOnWindows() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(WINDOWS));

		CapturingOverthereProcessOutputHandler captured = capturingHandler();
		int res = connection.execute(multiHandler(loggingHandler(logger), captured), CmdLine.build("ipconfig"));
		assertThat(res, equalTo(0));
		assertThat(captured.getOutput(), not(containsString("ipconfig")));
		assertThat(captured.getOutput(), containsString("Windows IP Configuration"));
	}

	@Test
	public void shouldExecuteSimpleCommandInWorkingDirectoryOnWindowsNotWithSftpCygwin() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(WINDOWS));
		assumeThat(connection.getClass().getName(), not(equalTo("com.xebialabs.overthere.ssh.SshSftpCygwinConnection")));

		connection.setWorkingDirectory(connection.getFile("C:\\WINDOWS"));
		CapturingOverthereProcessOutputHandler captured = capturingHandler();
		int res = connection.execute(multiHandler(loggingHandler(logger), captured), CmdLine.build("cd"));
		assertThat(res, equalTo(0));
		assertThat(captured.getOutput().toUpperCase(), containsString("C:\\WINDOWS"));
	}


	@Test
	public void shouldExecuteSimpleCommandInWorkingDirectoryOnWindowsWithSftpCygwin() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(WINDOWS));
		assumeThat(connection.getClass().getName(), equalTo("com.xebialabs.overthere.ssh.SshSftpCygwinConnection"));

		connection.setWorkingDirectory(connection.getFile("C:\\WINDOWS"));
		CapturingOverthereProcessOutputHandler captured = capturingHandler();
		int res = connection.execute(multiHandler(loggingHandler(logger), captured), CmdLine.build("pwd"));
		assertThat(res, equalTo(0));
		assertThat(captured.getOutput().toLowerCase(), containsString("/cygdrive/c/windows"));
	}

	@Test
	public void shouldStartProcessSimpleCommandOnWindows() throws IOException, InterruptedException {
		assumeThat(connection.getHostOperatingSystem(), equalTo(WINDOWS));
		assumeThat(connection.canStartProcess(), equalTo(true));

		OverthereProcess p = connection.startProcess(CmdLine.build("ipconfig"));
		try {
			String commandOutput = CharStreams.toString(new InputStreamReader(p.getStdout()));
			assertThat(p.waitFor(), equalTo(0));
			assertThat(commandOutput, not(containsString("ipconfig")));
			assertThat(commandOutput, containsString("Windows IP Configuration"));
		} finally {
			p.destroy();
		}
	}
	
	@Test
	public void shouldStartProcessSimpleCommandOnWindowsShouldThrowExceptionWhenNotSupported() throws IOException, InterruptedException {
		assumeThat(connection.getHostOperatingSystem(), equalTo(WINDOWS));
		assumeThat(connection.canStartProcess(), equalTo(false));

		try {
			connection.startProcess(CmdLine.build("ipconfig"));
			fail("Expected UnsupportedOperationException to be thrown");
		} catch(UnsupportedOperationException expected) {
		}
	}


	@Test
	public void shouldExecuteCommandWithArgumentOnWindows() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(WINDOWS));

		CapturingOverthereProcessOutputHandler capturingHandler = capturingHandler();
		int res = connection.execute(multiHandler(loggingHandler(logger), capturingHandler), CmdLine.build("ipconfig", "/all"));
		assertThat(res, equalTo(0));
		assertThat(capturingHandler.getOutput(), containsString("DHCP Server"));
	}

	@Test
	public void shoudNotExecuteIncorrectCommandOnWindows() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(WINDOWS));

		int res = connection.execute(loggingHandler(logger), CmdLine.build("this-command-does-not-exist"));
		assertThat(res, not(equalTo(0)));
	}

	@Test
	public void shouldNormalizeWindowsPathWithForwardSlashes() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(WINDOWS));

		OverthereFile file = connection.getFile("C:/Windows/System32");
		assertThat(file.getPath(), equalTo("C:\\Windows\\System32"));
	}

	@Test
	public void shouldCreateWriteReadAndRemoveTemporaryFile() throws IOException {
		final String prefix = "prefix";
		final String suffix = "suffix";
		final byte[] contents = ("Contents of the temporary file created at " + System.currentTimeMillis() + "ms since the epoch").getBytes();

		OverthereFile tempFile = connection.getTempFile(prefix, suffix);
		assertThat("Expected a non-null return value from HostConnection.getTempFile()", tempFile, notNullValue());
		assertThat("Expected name of temporary file to start with the prefix", tempFile.getName(), startsWith(prefix));
		assertThat("Expected name of temporary file to end with the suffix", tempFile.getName(), endsWith(suffix));
		assertThat("Expected temporary file to not exist yet", tempFile.exists(), equalTo(false));

		OutputStream outputStream = tempFile.getOutputStream();
		try {
			outputStream.write(contents);
		} finally {
			outputStream.close();
		}

		assertThat("Expected temporary file to exist after writing to it", tempFile.exists(), equalTo(true));
		assertThat("Expected temporary file to not be a directory", tempFile.isDirectory(), equalTo(false));
		assertThat("Expected temporary file to have the size of the contents written to it", tempFile.length(), equalTo((long) contents.length));
		assertThat("Expected temporary file to be readable", tempFile.canRead(), equalTo(true));
		assertThat("Expected temporary file to be writeable", tempFile.canWrite(), equalTo(true));

		// Windows systems don't support the concept of checking for executability
		if (connection.getHostOperatingSystem() == OperatingSystemFamily.UNIX) {
			assertFalse("Expected temporary file to not be executable", tempFile.canExecute());
		}

		DataInputStream inputStream = new DataInputStream(tempFile.getInputStream());
		try {
			final byte[] contentsRead = new byte[contents.length];
			inputStream.readFully(contentsRead);
			assertThat("Expected input stream to be exhausted after reading the full contents", inputStream.available(), equalTo(0));
			assertThat("Expected contents in temporary file to be identical to data written into it", contentsRead, equalTo(contents));
		} finally {
			inputStream.close();
		}

		tempFile.delete();
		assertThat("Expected temporary file to no longer exist", tempFile.exists(), equalTo(false));
	}

	@Test
	public void shouldCreatePopulateListAndRemoveTemporaryDirectory() throws IOException {
		final String prefix = "prefix";
		final String suffix = "suffix";

		OverthereFile tempDir = connection.getTempFile(prefix, suffix);
		assertThat("Expected a non-null return value from HostConnection.getTempFile()", tempDir, notNullValue());
		assertThat("Expected name of temporary file to start with the prefix", tempDir.getName(), startsWith(prefix));
		assertThat("Expected name of temporary file to end with the suffix", tempDir.getName(), endsWith(suffix));
		assertThat("Expected temporary file to not exist yet", tempDir.exists(), equalTo(false));

		tempDir.mkdir();
		assertThat("Expected temporary directory to exist after creating it", tempDir.exists(), equalTo(true));
		assertThat("Expected temporary directory to be a directory", tempDir.isDirectory(), equalTo(true));

		OverthereFile anotherTempDir = connection.getTempFile(prefix, suffix);
		assertThat("Expected temporary directories created with identical prefix and suffix to still be different", tempDir.getPath(), not(equalTo(anotherTempDir.getPath())));

		OverthereFile nested1 = tempDir.getFile("nested1");
		OverthereFile nested2 = nested1.getFile("nested2");
		OverthereFile nested3 = nested2.getFile("nested3");
		assertThat("Expected deeply nested directory to not exist", nested3.exists(), equalTo(false));
		try {
			nested3.mkdir();
			fail("Expected not to be able to create a deeply nested directory in one go");
		} catch (RuntimeIOException expected1) {
		}
		assertThat("Expected deeply nested directory to still not exist", nested3.exists(), equalTo(false));
		nested3.mkdirs();
		assertThat("Expected deeply nested directory to exist after invoking mkdirs on it", nested3.exists(), equalTo(true));

		final byte[] contents = ("Contents of the temporary file created at " + System.currentTimeMillis() + "ms since the epoch").getBytes();
		OverthereFile regularFile = tempDir.getFile("somefile.txt");
		OverthereUtils.write(contents, regularFile);

		List<OverthereFile> dirContents = tempDir.listFiles();
		assertThat("Expected directory to contain two entries", dirContents.size(), equalTo(2));
		assertThat("Expected directory to contain parent of deeply nested directory", dirContents.contains(nested1), equalTo(true));
		assertThat("Expected directory to contain regular file that was just created", dirContents.contains(regularFile), equalTo(true));

		try {
			nested1.delete();
		} catch (RuntimeIOException expected2) {
		}
		nested1.deleteRecursively();
		assertThat("Expected parent of deeply nested directory to have been removed recursively", nested1.exists(), equalTo(false));

		regularFile.delete();
		tempDir.delete();
		assertThat("Expected temporary directory to not exist after removing it when it was empty", tempDir.exists(), equalTo(false));
	}

	@Test
	public void shouldWriteLargeFile() throws IOException {
		byte[] largeFileContentsWritten = generateRandomBytes(LARGE_FILE_SIZE);
		
		OverthereFile remoteLargeFile = connection.getTempFile("large.dat");
		OverthereUtils.write(largeFileContentsWritten, remoteLargeFile);
		
		byte[] largeFileContentsRead = new byte[LARGE_FILE_SIZE];
		InputStream largeIn = remoteLargeFile.getInputStream();
		try {
			ByteStreams.readFully(largeIn, largeFileContentsRead);
		} finally {
			largeIn.close();
		}

		assertThat(largeFileContentsRead, equalTo(largeFileContentsWritten));
	}

	@Test
	public void shouldCopyLargeFile() throws IOException {
		File largeFile = temp.newFile("large.dat");
		byte[] largeFileContentsWritten = writeRandomBytes(largeFile, LARGE_FILE_SIZE);
		
		OverthereFile remoteLargeFile = connection.getTempFile("large.dat");
		LocalFile.valueOf(largeFile).copyTo(remoteLargeFile);
		
		byte[] largeFileContentsRead = new byte[LARGE_FILE_SIZE];
		InputStream largeIn = remoteLargeFile.getInputStream();
		try {
			ByteStreams.readFully(largeIn, largeFileContentsRead);
		} finally {
			largeIn.close();
		}

		assertThat(largeFileContentsRead, equalTo(largeFileContentsWritten));
	}

	@Test
	public void shouldCopyDirectoryWithManyFiles() throws IOException {
		File largeFolder = temp.newFolder("large.folder");
		for(int i = 0; i < NR_OF_SMALL_FILES; i++) {
			writeRandomBytes(new File(largeFolder, "large" + i + ".dat"), SMALL_FILE_SIZE);
		}

		OverthereFile remoteLargeFolder = connection.getTempFile("large.folder");
		LocalFile.valueOf(largeFolder).copyTo(remoteLargeFolder);	
	}

    @Test
	public void shouldCopyDirectoryContentToOtherLocation() throws IOException {
		OverthereFile tempDir = connection.getTempFile("tempdir");
        tempDir.mkdir();
        //Make sure targetFolder is not seen as a temporary folder. Sudo connection handles temp files differently.
        OverthereFile target = connection.getFile(tempDir.getPath() + "/targetFolder");

        OverthereFile sourceFolder = LocalFile.valueOf(temp.newFolder("sourceFolder"));
        OverthereFile sourceFile = sourceFolder.getFile("sourceFile");
        OverthereUtils.write("Some test data", "UTF-8", sourceFile);

        sourceFolder.copyTo(target);

        try {
            assertThat(target.getFile("sourceFile").exists(), is(true));
        } finally {
            // When using a sudo connection, the target folder has different rights to the temp folder it was created in.
            target.deleteRecursively();
        }
	}

    @Test
	public void shouldCopyDirectoryContentToExistingOtherLocation() throws IOException {
		OverthereFile tempDir = connection.getTempFile("tempdir");
        tempDir.mkdir();

        //Make sure targetFolder is not seen as a temporary folder. Sudo connection handles temp files differently.
        OverthereFile target = connection.getFile(tempDir.getPath() + "/targetFolder");
        target.mkdir();

        OverthereFile sourceFolder = LocalFile.valueOf(temp.newFolder("sourceFolder"));
        OverthereFile sourceFile = sourceFolder.getFile("sourceFile");
        OverthereUtils.write("Some test data", "UTF-8", sourceFile);

        sourceFolder.copyTo(target);

        try {
            assertThat(target.getFile("sourceFile").exists(), is(true));
        } finally {
            // When using a sudo connection, the target folder has different rights to the temp folder it was created in.
            target.deleteRecursively();
        }
	}

	/**
	 * Tests whether getOutputStream and getInputStream have the right permission behaviour (specifically for SSH/SUDO connections).
	 */
	@Test
	public void shouldWriteFileToAndReadFileFromSudoUserHomeDirectoryOnUnix() throws IOException {
		assumeThat(connection.getHostOperatingSystem(), equalTo(UNIX));
		assumeThat(type, not(equalTo(LOCAL_PROTOCOL)));

		// get handle to file in home dir
		final OverthereFile homeDir = connection.getFile(getUnixHomeDirPath());
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
	public void shouldCopyFileToAndFromSudoUserHomeDirectoryOnUnix() throws IOException {
		assumeThat(connection.getHostOperatingSystem(), equalTo(UNIX));
		assumeThat(type, not(equalTo(LOCAL_PROTOCOL)));

		// get handle to file in home dir
		final OverthereFile homeDir = connection.getFile(getUnixHomeDirPath());
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

	protected String getUnixHomeDirPath() {
		String sudoUsername = options.getOptional(SUDO_USERNAME);
		if(sudoUsername != null) {
			return "/home/" + sudoUsername;
		} else {
			return "/home/" + options.get(USERNAME);
		}
    }

	@Test
	public void shouldSetExecutableOnUnix() {
		assumeThat(connection.getHostOperatingSystem(), equalTo(UNIX));

		OverthereFile remoteFile = connection.getTempFile("executable.sh");
		OverthereUtils.write(generateRandomBytes(256), remoteFile);
		
		assertThat(remoteFile.canExecute(), equalTo(false));
		remoteFile.setExecutable(true);
		assertThat(remoteFile.canExecute(), equalTo(true));
		remoteFile.setExecutable(false);
		assertThat(remoteFile.canExecute(), equalTo(false));
	}

	@Test
	public void shouldTruncateExistingTargetFileOnCopy() throws Exception {
		final OverthereFile existingDestination = connection.getTempFile("existing");
		writeData(existingDestination, "**********\n**********\n**********\n**********\n**********\n".getBytes());
		final OverthereFile newSource = connection.getTempFile("newContents");
		writeData(newSource, "++++++++++".getBytes());
		newSource.copyTo(existingDestination);

		ByteArrayOutputStream to = new ByteArrayOutputStream();
		ByteStreams.copy(new InputSupplier<InputStream>() {
			@Override
			public InputStream getInput() throws IOException {
				return existingDestination.getInputStream();
			}
		}, to);
		byte[] bytes = to.toByteArray();
		assertThat(bytes.length, equalTo(10));
		assertThat(bytes, equalTo("++++++++++".getBytes()));
	}

	private void writeData(final OverthereFile tempFile, byte[] data) throws IOException {
		ByteStreams.write(data, new OutputSupplier<OutputStream>() {
			@Override
			public OutputStream getOutput() throws IOException {
				return tempFile.getOutputStream();
			}
		});
	}


	protected static byte[] writeRandomBytes(final File f, final int size) throws IOException {
		byte[] randomBytes = generateRandomBytes(size);
		ByteStreams.write(randomBytes, new OutputSupplier<OutputStream>() {
			@Override
            public OutputStream getOutput() throws IOException {
				return new FileOutputStream(f);
            }
		});
		return randomBytes;
	}

	protected static byte[] generateRandomBytes(final int size) {
	    byte[] randomBytes = new byte[size];
		new Random().nextBytes(randomBytes);
	    return randomBytes;
    }

	private static Logger logger = LoggerFactory.getLogger(OverthereConnectionItestBase.class);

}

