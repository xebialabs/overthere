/*
 * Copyright (c) 2008-2014, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;

import com.xebialabs.overthere.local.LocalFile;
import com.xebialabs.overthere.ssh.SshConnectionType;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

import nl.javadude.assumeng.Assumption;
import nl.javadude.assumeng.AssumptionListener;

import static com.google.common.io.ByteStreams.copy;
import static com.google.common.io.ByteStreams.readFully;
import static com.google.common.io.ByteStreams.toByteArray;
import static com.google.common.io.ByteStreams.write;
import static com.google.common.io.Closeables.closeQuietly;
import static com.xebialabs.overthere.CmdLineArgument.SPECIAL_CHARS_UNIX;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static com.xebialabs.overthere.local.LocalConnection.getLocalConnection;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.ConsoleOverthereExecutionOutputHandler.syserrHandler;
import static com.xebialabs.overthere.util.ConsoleOverthereExecutionOutputHandler.sysoutHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingErrorHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingOutputHandler;
import static com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler.multiHandler;
import static java.lang.String.format;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.testng.Assert.fail;

/**
 * Base class for all Overthere connection itests.
 */
@Listeners(AssumptionListener.class)
public abstract class OverthereConnectionItestBase {

    public static final int NR_OF_SMALL_FILES = 256;
    public static final int SMALL_FILE_SIZE = 10 * 1024;
    public static final int LARGE_FILE_SIZE = 1 * 1024 * 1024;
    /* FIXME: Set LARGE_FILE_SIZE back to 100MB when running the itest on a local KVM host. */

    protected TemporaryFolder temp = new TemporaryFolder();
    protected String protocol;
    protected ConnectionOptions options;
    protected String expectedConnectionClassName;
    protected OverthereConnection connection;

    protected abstract String getProtocol();

    protected abstract ConnectionOptions getOptions();

    protected abstract String getExpectedConnectionClassName();

    @BeforeClass
    public void setupHost() throws Exception {
        temp.create();

        protocol = getProtocol();
        options = getOptions();
        expectedConnectionClassName = getExpectedConnectionClassName();

        connection = Overthere.getConnection(protocol, options);
    }

    @AfterClass(alwaysRun = true)
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (Exception exc) {
                System.out.println("Exception while disconnecting at end of test case:");
                exc.printStackTrace(System.out);
            } catch (AssertionError e) {
                System.out.println("Ignoring " + e);
            }
        }
        temp.delete();
    }

    @BeforeMethod
    public void assertConnection() {
        assertThat("We're not connected!", connection != null);
    }

    @Test
    public void connectionObjectShouldBeInstanceOfExpectedClass() {
        assertThat(connection.getClass().getName(), equalTo(expectedConnectionClassName));
    }

    @Test
    @Assumption(methods = {"notLocal", "notCifs"})
    public void shouldNotConnectWithIncorrectUsername() {
        ConnectionOptions incorrectUserNameOptions = new ConnectionOptions(options);
        incorrectUserNameOptions.set(USERNAME, "an-incorrect-username");
        try {
            Overthere.getConnection(protocol, incorrectUserNameOptions);
            fail("Expected not to be able to connect with an incorrect username");
        } catch (RuntimeIOException expected) {
        }
    }

    @Test
    @Assumption(methods = {"onWindows", "onlyCifsWinrm"})
    public void shouldThrowValidationMessageWhenTryingToConnectWithOldStyleWindowsDomainAccount() {
        ConnectionOptions incorrectUserNameOptions = new ConnectionOptions(options);
        incorrectUserNameOptions.set(USERNAME, "DOMAIN\\user");
        try {
            Overthere.getConnection(protocol, incorrectUserNameOptions);
            fail("Expected not to be able to connect with an old-style Windows domain account");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Cannot start a " + CIFS_PROTOCOL + ":" + WINRM_INTERNAL.toString().toLowerCase() + " connection with an old-style Windows domain account"));
        }
    }

    @Test
    @Assumption(methods = {"onWindows", "onlyCifsTelnet"})
    public void shouldThrowValidationMessageWhenTryingToConnectWithNewStyleWindowsDomainAccount() {
        ConnectionOptions incorrectUserNameOptions = new ConnectionOptions(options);
        incorrectUserNameOptions.set(USERNAME, "user@DOMAIN");
        try {
            Overthere.getConnection(protocol, incorrectUserNameOptions);
            fail("Expected not to be able to connect with a new-style Windows domain account");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Cannot start a " + CIFS_PROTOCOL + ":" + TELNET.toString().toLowerCase() + " connection with a new-style Windows domain account"));
        }
    }

    @Test
    @Assumption(methods = {"notLocal", "notCifs", "withPassword"})
    public void shouldNotConnectWithIncorrectPassword() {
        ConnectionOptions incorrectPasswordOptions = new ConnectionOptions(options);
        incorrectPasswordOptions.set(PASSWORD, "an-incorrect-password");
        try {
            Overthere.getConnection(protocol, incorrectPasswordOptions);
            fail("Expected not to be able to connect with an incorrect password");
        } catch (RuntimeIOException expected) {
        }
    }

    @Test
    @Assumption(methods = "onUnix")
    public void shouldExecuteSimpleCommandOnUnix() {
        CapturingOverthereExecutionOutputHandler captured = capturingHandler();
        int res = connection.execute(multiHandler(sysoutHandler(), captured), syserrHandler(), CmdLine.build("ls", "-ld", "/tmp/."));
        assertThat(res, equalTo(0));
        assertThat(captured.getOutput(), containsString("drwxrwxrwt"));
    }

    @Test
    @Assumption(methods = "onUnix")
    public void shouldExecuteCommandWithSpecialCharactersOnUnix() {
        CapturingOverthereExecutionOutputHandler captured = capturingHandler();
        int res = connection.execute(multiHandler(sysoutHandler(), captured), syserrHandler(), CmdLine.build("echo", SPECIAL_CHARS_UNIX));
        assertThat(res, equalTo(0));
        for (int i = 0; i < SPECIAL_CHARS_UNIX.length(); i++) {
            assertThat(captured.getOutput().indexOf(SPECIAL_CHARS_UNIX.charAt(i)) >= 0, is(true));
        }
    }

    @Test
    @Assumption(methods = "onUnix")
    public void shouldExecuteSimpleCommandInWorkingDirectoryOnUnix() {

        connection.setWorkingDirectory(connection.getFile("/etc"));
        CapturingOverthereExecutionOutputHandler captured = capturingHandler();
        int res = connection.execute(multiHandler(sysoutHandler(), captured), syserrHandler(), CmdLine.build("pwd"));
        assertThat(res, equalTo(0));
        assertThat(captured.getOutput(), containsString("/etc"));
    }

    @Test
    @Assumption(methods = "onUnix")
    public void shouldCaptureLastLineOfSimpleCommandOnUnix() {
        CapturingOverthereExecutionOutputHandler captured = capturingHandler();
        int res = connection.execute(multiHandler(sysoutHandler(), captured), syserrHandler(),
                CmdLine.build("echo", "-n", "line", "that", "does", "not", "end", "in", "a", "newline"));
        assertThat(res, equalTo(0));
        if (captured.getOutputLines().size() > 1) {
            // When using ssh_interactive_sudo, the output may be proceeded by the password prompt and possibly even the
            // sudo warning message.
            assertThat(captured.getOutputLines().get(captured.getOutputLines().size() - 2), containsString("assword"));
            assertThat(captured.getOutputLines().get(captured.getOutputLines().size() - 1), containsString("line that does not end in a newline"));
        } else {
            assertThat(captured.getOutputLines().size(), equalTo(1));
            assertThat(captured.getOutput(), containsString("line that does not end in a newline"));
        }
    }

    @Test(enabled = false, description = "Test fails for WinRM because startProcess does not throw an UnsupportedOperationException even though canStartProcess returns false. Enable test when WinRM streaming is properly implemented.")
    @Assumption(methods = {"notSupportsProcess"})
    public void shouldStartProcessShouldThrowExceptionWhenNotSupported() {
        try {
            connection.startProcess(CmdLine.build("echo"));
            fail("Expected UnsupportedOperationException to be thrown");
        } catch (UnsupportedOperationException expected) {
        }
    }

    @Test
    @Assumption(methods = "onUnix")
    public void shouldStartProcessSimpleCommandOnUnix() throws IOException, InterruptedException {
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
    @Assumption(methods = "onWindows")
    public void shouldListFilesOnWindows() {
        OverthereFile folder = connection.getFile("C:\\overthere");
        List<OverthereFile> filesInFolder = folder.listFiles();

        OverthereFile expectedFile = connection.getFile("C:\\overthere\\overhere.txt");
        assertThat(filesInFolder.contains(expectedFile), equalTo(true));
    }

    @Test
    @Assumption(methods = "onWindows")
    public void shouldExecuteSimpleCommandOnWindows() {
        CapturingOverthereExecutionOutputHandler captured = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), captured), loggingErrorHandler(logger), CmdLine.build("ipconfig"));
        assertThat(res, equalTo(0));
        assertThat(captured.getOutput(), not(containsString("ipconfig")));
        assertThat(captured.getOutput(), containsString("Windows IP Configuration"));
    }

    @Test
    @Assumption(methods = {"onWindows", "notSftpCygwin"})
    public void shouldExecuteSimpleCommandInWorkingDirectoryOnWindowsNotWithSftpCygwin() {
        connection.setWorkingDirectory(connection.getFile("C:\\WINDOWS"));
        CapturingOverthereExecutionOutputHandler captured = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), captured), loggingErrorHandler(logger), CmdLine.build("cd"));
        assertThat(res, equalTo(0));
        assertThat(captured.getOutput().toUpperCase(), containsString("C:\\WINDOWS"));
    }

    @Test
    @Assumption(methods = {"onWindows", "onlySftpCygwin"})
    public void shouldExecuteSimpleCommandInWorkingDirectoryOnWindowsWithSftpCygwin() {
        connection.setWorkingDirectory(connection.getFile("C:\\WINDOWS"));
        CapturingOverthereExecutionOutputHandler captured = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), captured), loggingErrorHandler(logger), CmdLine.build("pwd"));
        assertThat(res, equalTo(0));
        assertThat(captured.getOutput().toLowerCase(), containsString("/cygdrive/c/windows"));
    }

    @Test
    @Assumption(methods = {"onWindows", "supportsProcess"})
    public void shouldStartProcessSimpleCommandOnWindows() throws IOException, InterruptedException {
        OverthereProcess p = connection.startProcess(CmdLine.build("ipconfig"));
        try {
            String commandOutput = CharStreams.toString(new InputStreamReader(p.getStdout()));
            assertThat(p.waitFor(), equalTo(0));
            assertThat(commandOutput, not(containsString("ipconfig")));
            assertThat(commandOutput, containsString("Windows IP Configuration"));
        } finally {
            p.waitFor();
        }
    }

    @Test
    @Assumption(methods = {"onWindows", "supportsProcess", "notSftpCygwin", "notSftpWinsshd"})
    public void shouldStartProcessInteractiveCommandOnWindows() throws IOException, InterruptedException {
        OverthereProcess process = connection.startProcess(CmdLine.build("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-File", "C:\\overthere\\echoname.ps1"));
        try {
            OutputStream stdin = process.getStdin();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getStdout()));

            waitForPrompt(stdout, "name");

            String reply = "iiPoWeR";
            enterPrompt(stdin, reply);

            String hi = waitForPrompt(stdout, "Hi");
            assertThat(hi, containsString("iiPoWeR"));
        } finally {
            process.waitFor();
        }
    }

    private String waitForPrompt(BufferedReader stdout, String prompt) throws IOException {
        for (; ; ) {
            String line = stdout.readLine();
            assertThat(line, not(nullValue()));
            if (line.contains(prompt))
                return line;
        }
    }

    private void enterPrompt(OutputStream stdin, String reply) throws IOException {
        stdin.write((reply + "\r\n").getBytes());
        stdin.flush();
    }

    @Test
    @Assumption(methods = {"onWindows"})
    public void shouldExecuteCommandWithArgumentOnWindows() {
        CapturingOverthereExecutionOutputHandler capturingHandler = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), capturingHandler), loggingErrorHandler(logger), CmdLine.build("ipconfig", "/all"));
        assertThat(res, equalTo(0));
        assertThat(capturingHandler.getOutput(), containsString("Windows IP Configuration"));
    }

    @Test
    @Assumption(methods = {"onWindows"})
    public void shouldExecuteBatchFileOnWindows() {
        CapturingOverthereExecutionOutputHandler capturingHandler = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), capturingHandler), loggingErrorHandler(logger), CmdLine.build("C:\\overthere\\helloworld.bat"));
        assertThat(res, equalTo(0));
        assertThat(capturingHandler.getOutput(), containsString("Hello World"));
    }

    @Test
    @Assumption(methods = {"onWindows"})
    public void shouldExecuteBatchFileWithArgumentsOnWindows() throws IOException {
        String content = "Hello from the file just uploaded";
        OverthereFile tempFile = connection.getTempFile("hello world", ".txt");
        OutputStream out = tempFile.getOutputStream();
        try {
            out.write(content.getBytes());
        } finally {
            closeQuietly(out);
        }

        CapturingOverthereExecutionOutputHandler capturingHandler = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), capturingHandler), loggingErrorHandler(logger), CmdLine.build("C:\\overthere\\typefile.bat", tempFile.getPath()));
        assertThat(res, equalTo(0));
        assertThat(capturingHandler.getOutput(), containsString(content));
    }

    @Test
    @Assumption(methods = {"onWindows"})
    public void shouldNotExecuteIncorrectCommandOnWindows() {
        int res = connection.execute(loggingOutputHandler(logger), loggingErrorHandler(logger), CmdLine.build("this-command-does-not-exist"));
        assertThat(res, not(equalTo(0)));
    }

    @Test
    @Assumption(methods = {"onWindows"})
    public void shouldNormalizeWindowsPathWithForwardSlashes() {
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

        OutputStream out = tempFile.getOutputStream();
        try {
            out.write(contents);
        } finally {
            closeQuietly(out);
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

        DataInputStream in = new DataInputStream(tempFile.getInputStream());
        try {
            final byte[] contentsRead = new byte[contents.length];
            in.readFully(contentsRead);
            assertThat("Expected input stream to be exhausted after reading the full contents", in.available(), equalTo(0));
            assertThat("Expected contents in temporary file to be identical to data written into it", contentsRead, equalTo(contents));
        } finally {
            closeQuietly(in);
        }

        tempFile.delete();
        assertThat("Expected temporary file to no longer exist", tempFile.exists(), equalTo(false));
    }

    @Test
    @Assumption(methods = "onUnix")
    public void shouldCreateHiddenUnixFileAndBeAbleToListIt() {
        final String prefix = "prefix";
        final String suffix = "suffix";
        OverthereFile tempDir = connection.getTempFile(prefix, suffix);
        tempDir.mkdir();
        byte[] contents = "Hey there, I'm a hidden file.".getBytes();
        OverthereFile hiddenFile = tempDir.getFile(".imhidden");
        OverthereUtils.write(contents, hiddenFile);
        List<OverthereFile> overthereFiles = tempDir.listFiles();
        assertThat("Expected dir listing to list hidden file.", overthereFiles, hasSize(1));
        hiddenFile.delete();
        assertThat("Should have removed hidden file", !hiddenFile.exists());
        tempDir.delete();
    }

    @Test
    public void shouldCreatePopulateListAndRemoveTemporaryDirectory() {
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
        assertThat("Expected temporary directories created with identical prefix and suffix to still be different", tempDir.getPath(),
                not(equalTo(anotherTempDir.getPath())));

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

        assertThat(readFile(remoteLargeFile), equalTo(largeFileContentsWritten));
    }

    @Test
    public void shouldCopyLargeFile() throws IOException {
        File largeFile = temp.newFile("large.dat");
        byte[] largeFileContentsWritten = writeRandomBytes(largeFile, LARGE_FILE_SIZE);

        OverthereFile remoteLargeFile = connection.getTempFile("large.dat");
        LocalFile.valueOf(largeFile).copyTo(remoteLargeFile);

        assertThat(readFile(remoteLargeFile), equalTo(largeFileContentsWritten));
    }

    @Test
    public void shouldCopyFileWithSpaceNameToNonTempLocation() throws IOException {
        File fileWithSpaces = temp.newFile("I have spaces.txt");
        writeRandomBytes(fileWithSpaces, 100);

        OverthereFile dir = connection.getTempFile("dir");
        OverthereFile targetDir = connection.getFile(dir.getPath() + "/newDir");
        targetDir.mkdirs();

        OverthereFile targetFile = connection.getFile(targetDir.getPath() + "/" + fileWithSpaces.getName());

        LocalFile.valueOf(fileWithSpaces).copyTo(targetFile);
        try {
            assertThat(targetFile.exists(), is(true));
        } finally {
            // When using a sudo connection, the target folder has different rights to the temp folder it was created in.
            targetDir.deleteRecursively();
        }
    }

    @Test
    public void shouldCopyDirectoryWithManyFiles() throws IOException {
        File largeFolder = temp.newFolder("small.folder");
        for (int i = 0; i < NR_OF_SMALL_FILES; i++) {
            writeRandomBytes(new File(largeFolder, "small" + i + ".dat"), SMALL_FILE_SIZE);
        }

        OverthereFile remoteLargeFolder = connection.getTempFile("small.folder");
        LocalFile.valueOf(largeFolder).copyTo(remoteLargeFolder);

        for (int i = 0; i < NR_OF_SMALL_FILES; i++) {
            OverthereFile remoteFile = remoteLargeFolder.getFile("small" + i + ".dat");
            byte[] remoteBytes = OverthereUtils.read(remoteFile);
            assertThat(remoteBytes.length, equalTo(SMALL_FILE_SIZE));
        }
    }

    @Test
    public void shouldCopyLocalFileToNonExistentRemoteFile() {
        OverthereFile srcFile = getLocalSourceFile();
        OverthereFile dstFile = getRemoteDestinationFile();

        populateSourceFile(srcFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyLocalFileToExistentRemoteFile() {
        OverthereFile srcFile = getLocalSourceFile();
        OverthereFile dstFile = getRemoteDestinationFile();

        populateSourceFile(srcFile);
        populateExistentDestinationFile(dstFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyLocalFileToNonExistentRemoteFileWithDifferentName() {
        OverthereFile srcFile = getLocalSourceFile();
        OverthereFile dstFile = getRemoteDestinationFileWithDifferentName();

        populateSourceFile(srcFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyLocalFileToExistentRemoteFileWithDifferentName() {
        OverthereFile srcFile = getLocalSourceFile();
        OverthereFile dstFile = getRemoteDestinationFileWithDifferentName();

        populateSourceFile(srcFile);
        populateExistentDestinationFile(dstFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyRemoteFileToNonExistentRemoteFile() {
        OverthereFile srcFile = getRemoteSourceFile();
        OverthereFile dstFile = getRemoteDestinationFile();

        populateSourceFile(srcFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyRemoteFileToExistentRemoteFile() {
        OverthereFile srcFile = getRemoteSourceFile();
        OverthereFile dstFile = getRemoteDestinationFile();

        populateSourceFile(srcFile);
        populateExistentDestinationFile(dstFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyRemoteFileToNonExistentRemoteFileWithDifferentName() {
        OverthereFile srcFile = getRemoteSourceFile();
        OverthereFile dstFile = getRemoteDestinationFileWithDifferentName();

        populateSourceFile(srcFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyRemoteFileToExistentRemoteFileWithDifferentName() {
        OverthereFile srcFile = getLocalSourceFile();
        OverthereFile dstFile = getRemoteDestinationFileWithDifferentName();

        populateSourceFile(srcFile);
        populateExistentDestinationFile(dstFile);

        srcFile.copyTo(dstFile);

        assertSourceFileWasCopiedToDestinationFile(dstFile);
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectory() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectory();

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectory() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectory();

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectoryWithDifferentName() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectoryWithDifferentName();

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectoryWithDifferentName() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectoryWithDifferentName();

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectory() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectory();

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectory() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectory();

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectoryWithDifferentName() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectoryWithDifferentName();

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectoryWithDifferentName() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = getRemoteDestinationDirectoryWithDifferentName();

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectoryWithTrailingPathSeparator() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = addPathSeparator(getRemoteDestinationDirectory());

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectoryWithTrailingPathSeparator() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = addPathSeparator(getRemoteDestinationDirectory());

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToNonExistentRemoteDirectoryWithDifferentNameAndTrainlingPathSeparator() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = addPathSeparator(getRemoteDestinationDirectoryWithDifferentName());

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyLocalDirectoryToExistentRemoteDirectoryWithDifferentNameAndTrainlingPathSeparator() {
        OverthereFile srcDir = getLocalSourceDirectory();
        OverthereFile dstDir = addPathSeparator(getRemoteDestinationDirectoryWithDifferentName());

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectoryWithTrailingPathSeparator() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = addPathSeparator(getRemoteDestinationDirectory());

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectoryWithTrailingPathSeparator() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = addPathSeparator(getRemoteDestinationDirectory());

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToNonExistentRemoteDirectoryWithDifferentNameAndTrainlingPathSeparator() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = addPathSeparator(getRemoteDestinationDirectoryWithDifferentName());

        populateSourceDirectory(srcDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(dstDir);
    }

    @Test
    public void shouldCopyRemoteDirectoryToExistentRemoteDirectoryWithDifferentNameAndTrainlingPathSeparator() {
        OverthereFile srcDir = getRemoteSourceDirectory();
        OverthereFile dstDir = addPathSeparator(getRemoteDestinationDirectoryWithDifferentName());

        populateSourceDirectory(srcDir);
        populateExistentDestinationDirectory(dstDir);

        srcDir.copyTo(dstDir);

        assertSourceDirectoryWasCopiedToExistentDestinationDirectory(dstDir);
    }



    private static final String SOURCE_DIR_NAME = "dir-to-copy";

    private static final String DESTINATION_DIR_ALTERNATIVE_NAME = "dir-to-copy-with-different-name";

    private static byte[] SOURCE_FILE_CONTENTS = "This file should be copied".getBytes();

    private static final String SOURCE_FILE_NAME = "file-to-copy.txt";

    private static final String DESTINATION_FILE_ALTERNATIVE_NAME = "file-to-copy-with-different-name.txt";

    private static byte[] EXISTENT_DEST_FILE_CONTENT = "This file should be overwritten".getBytes();

    private static final String OTHER_DEST_FILE_NAME = "file-to-be-left-as-is.txt";

    private static byte[] OTHER_DEST_FILE_CONTENT = "This should be left as-is".getBytes();

    private OverthereFile getLocalSourceFile() {
        return getLocalConnection().getTempFile(SOURCE_FILE_NAME);
    }

    private OverthereFile getRemoteSourceFile() {
        return connection.getTempFile(SOURCE_FILE_NAME);
    }

    private OverthereFile getRemoteDestinationFile() {
        return connection.getTempFile(SOURCE_FILE_NAME);
    }

    private OverthereFile getRemoteDestinationFileWithDifferentName() {
        return connection.getTempFile(DESTINATION_FILE_ALTERNATIVE_NAME);
    }

    private void populateSourceFile(final OverthereFile srcFile) {
        writeData(srcFile, SOURCE_FILE_CONTENTS);
    }

    private void populateExistentDestinationFile(final OverthereFile dstFile) {
        writeData(dstFile, EXISTENT_DEST_FILE_CONTENT);
    }

    private void assertSourceFileWasCopiedToDestinationFile(final OverthereFile dstFile) {
        assertFile(dstFile, SOURCE_FILE_CONTENTS);
    }

    private OverthereFile getLocalSourceDirectory() {
        return getLocalConnection().getTempFile(SOURCE_DIR_NAME);
    }

    private OverthereFile getRemoteSourceDirectory() {
        return connection.getTempFile(SOURCE_DIR_NAME);
    }

    private OverthereFile getRemoteDestinationDirectory() {
        return connection.getTempFile(SOURCE_DIR_NAME);
    }

    private OverthereFile getRemoteDestinationDirectoryWithDifferentName() {
        return connection.getTempFile(DESTINATION_DIR_ALTERNATIVE_NAME);
    }

    private OverthereFile addPathSeparator(OverthereFile dir) {
        return connection.getFile(dir.getPath() + connection.getHostOperatingSystem().getPathSeparator());
    }

    private void populateSourceDirectory(final OverthereFile srcDir) {
        srcDir.mkdir();
        OverthereFile fileInSrcDir = srcDir.getFile(SOURCE_FILE_NAME);
        writeData(fileInSrcDir, SOURCE_FILE_CONTENTS);
    }

    private void populateExistentDestinationDirectory(final OverthereFile dstDir) {
        dstDir.mkdir();
        OverthereFile fileInDestDir = dstDir.getFile(SOURCE_FILE_NAME);
        writeData(fileInDestDir, EXISTENT_DEST_FILE_CONTENT);
        OverthereFile otherFileInDestDir = dstDir.getFile(OTHER_DEST_FILE_NAME);
        writeData(otherFileInDestDir, OTHER_DEST_FILE_CONTENT);
    }

    private void assertSourceDirectoryWasCopiedToNonExistentDestinationDirectory(final OverthereFile dstDir) {
        assertDir(dstDir);
        assertFile(dstDir.getFile(SOURCE_FILE_NAME), SOURCE_FILE_CONTENTS);
        assertThat(dstDir.getFile(OTHER_DEST_FILE_NAME).exists(), is(false));
    }

    private void assertSourceDirectoryWasCopiedToExistentDestinationDirectory(final OverthereFile dstDir) {
        assertDir(dstDir);
        assertFile(dstDir.getFile(SOURCE_FILE_NAME), SOURCE_FILE_CONTENTS);
        assertFile(dstDir.getFile(OTHER_DEST_FILE_NAME), OTHER_DEST_FILE_CONTENT);
    }

    private void assertDir(final OverthereFile dir) {
        assertThat(format("Directory [%s] does not exist", dir), dir.exists(), is(true));
        assertThat(format("Directory [%s] is not a directory", dir), dir.isDirectory(), is(true));
    }

    private void assertFile(final OverthereFile file, final byte[] expectedContents) {
        assertThat(format("File [%s] does not exist", file), file.exists(), is(true));
        assertThat(format("File [%s] is not a regular file", file), file.isFile(), is(true));
        byte[] actualContents = readFile(file);
        assertThat(format("File [%s] does not have the expected contents", file), Arrays.equals(actualContents, expectedContents), is(true));
    }

    private byte[] readFile(final OverthereFile file) {
        try {
            return toByteArray(new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    return file.getInputStream();
                }
            });
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot read file [%s]", file), exc);
        }

    }

    @Test
    @Assumption(methods = {"onUnix", "notLocal"})
    public void shouldWriteFileToAndReadFileFromSudoUserHomeDirectoryOnUnix() throws IOException {
        // get handle to file in home dir
        final OverthereFile homeDir = connection.getFile(getUnixHomeDirPath());
        final OverthereFile fileInHomeDir = homeDir.getFile("file" + System.currentTimeMillis() + ".dat");
        assertThat(fileInHomeDir.exists(), equalTo(false));

        // write data to file in home dir
        final byte[] contentsWritten = generateRandomBytes(SMALL_FILE_SIZE);
        write(contentsWritten, new OutputSupplier<OutputStream>() {
            @Override
            public OutputStream getOutput() throws IOException {
                return fileInHomeDir.getOutputStream();
            }
        });

        assertThat(fileInHomeDir.exists(), equalTo(true));

        // restrict access to file in home dir
        connection.execute(sysoutHandler(), syserrHandler(), CmdLine.build("chmod", "0600", fileInHomeDir.getPath()));

        // read file from home dir
        byte[] contentsRead = new byte[SMALL_FILE_SIZE];
        InputStream in = fileInHomeDir.getInputStream();
        try {
            readFully(in, contentsRead);
        } finally {
            closeQuietly(in);
        }
        assertThat(contentsRead, equalTo(contentsWritten));

        // restrict access to file in home dir
        fileInHomeDir.delete();
        assertThat(fileInHomeDir.exists(), equalTo(false));
    }

    @Test
    @Assumption(methods = {"onUnix", "notLocal"})
    public void shouldCopyFileToAndFromSudoUserHomeDirectoryOnUnix() throws IOException {
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
        connection.execute(sysoutHandler(), syserrHandler(), CmdLine.build("chmod", "0600", fileInHomeDir.getPath()));

        // copy file in home dir to local file
        File smallFileReadBack = temp.newFile("small-read-back.dat");
        OverthereFile smallLocalFileReadBack = LocalFile.valueOf(smallFileReadBack);
        fileInHomeDir.copyTo(smallLocalFileReadBack);

        // read new local file
        byte[] contentsRead = new byte[SMALL_FILE_SIZE];
        InputStream in = smallLocalFileReadBack.getInputStream();
        try {
            readFully(in, contentsRead);
        } finally {
            closeQuietly(in);
        }
        assertThat(contentsRead, equalTo(contentsWritten));

        // remove file from home dir
        fileInHomeDir.delete();
        assertThat(fileInHomeDir.exists(), equalTo(false));
    }

    protected String getUnixHomeDirPath() {
        String sudoUsername = options.getOptional(SUDO_USERNAME);
        if (sudoUsername != null) {
            return "/home/" + sudoUsername;
        } else {
            return "/home/" + options.get(USERNAME);
        }
    }

    @Test
    @Assumption(methods = "onUnix")
    public void shouldSetExecutableOnUnix() {
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
        copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                return existingDestination.getInputStream();
            }
        }, to);
        byte[] bytes = to.toByteArray();
        assertThat(bytes.length, equalTo(10));
        assertThat(bytes, equalTo("++++++++++".getBytes()));
    }

    private static void writeData(final OverthereFile destFile, byte[] data) {
        try {
            write(data, new OutputSupplier<OutputStream>() {
                @Override
                public OutputStream getOutput() throws IOException {
                    return destFile.getOutputStream();
                }
            });
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot write data to %s", destFile), exc);
        }
    }

    protected static byte[] writeRandomBytes(final File f, final int size) throws IOException {
        byte[] randomBytes = generateRandomBytes(size);
        write(randomBytes, new OutputSupplier<OutputStream>() {
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

    public boolean notLocal() {
        return !protocol.equals(LOCAL_PROTOCOL);
    }

    public boolean notCifs() {
        return !protocol.equals(CIFS_PROTOCOL);
    }

    public boolean withPassword() {
        return options.containsKey("password");
    }

    public boolean onUnix() {
        return connection.getHostOperatingSystem().equals(UNIX);
    }

    public boolean onWindows() {
        return connection.getHostOperatingSystem().equals(WINDOWS);
    }

    public boolean onlyCifs() {
        return protocol.equals(CIFS_PROTOCOL);
    }

    public boolean onlyCifsWinrm() {
        return protocol.equals(CIFS_PROTOCOL) && options.get(CONNECTION_TYPE).equals(WINRM_INTERNAL);
    }

    public boolean onlyCifsTelnet() {
        return protocol.equals(CIFS_PROTOCOL) && options.get(CONNECTION_TYPE).equals(TELNET);
    }

    public boolean notSftpCygwin() {
        return !onlySftpCygwin();
    }

    public boolean onlySftpCygwin() {
        return SshConnectionType.SFTP_CYGWIN.equals(options.get(CONNECTION_TYPE, null));
    }

    public boolean notSftpWinsshd() {
        return !onlySftpWinsshd();
    }

    public boolean onlySftpWinsshd() {
        return SshConnectionType.SFTP_WINSSHD.equals(options.get(CONNECTION_TYPE, null));
    }

    public boolean supportsProcess() {
        return connection.canStartProcess();
    }

    public boolean notSupportsProcess() {
        return !supportsProcess();
    }

    private static Logger logger = LoggerFactory.getLogger(OverthereConnectionItestBase.class);

}
