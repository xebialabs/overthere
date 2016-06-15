/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.itest;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import org.testng.annotations.Test;
import com.google.common.io.CharStreams;
import com.google.common.io.OutputSupplier;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.local.LocalFile;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

import nl.javadude.assumeng.Assumption;

import static com.google.common.io.ByteStreams.write;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.ConsoleOverthereExecutionOutputHandler.syserrHandler;
import static com.xebialabs.overthere.util.ConsoleOverthereExecutionOutputHandler.sysoutHandler;
import static com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler.multiHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

public abstract class ItestsBase5Unix extends ItestsBase4Size {

    /**
     * String containing special characters that require quoting or escaping on Unix.
     */
    public static final String SPECIAL_CHARS_UNIX = " '\"\\;&|()${}*?!<>";

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
    @Assumption(methods = {"onUnix", "notLocal"})
    public void shouldWriteFileToAndReadFileFromSudoUserHomeDirectoryOnUnix() throws IOException {
        // get handle to file in home dir
        final OverthereFile homeDir = connection.getFile(getUnixHomeDirPath());
        final OverthereFile fileInHomeDir = homeDir.getFile("file" + System.currentTimeMillis() + ".dat");
        assertThat(fileInHomeDir.exists(), equalTo(false));

        // write data to file in home dir
        final byte[] contentsWritten = generateRandomBytes(1024);
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
        byte[] contentsRead = readFile(fileInHomeDir);
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
        byte[] contentsWritten = writeRandomBytes(smallFile, 1024);
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
        byte[] contentsRead = readFile(smallLocalFileReadBack);
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


}
