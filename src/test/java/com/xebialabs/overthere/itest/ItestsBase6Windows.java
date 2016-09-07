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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.testng.annotations.Test;
import com.google.common.io.CharStreams;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;

import nl.javadude.assumeng.Assumption;

import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.SMB_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingErrorHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingOutputHandler;
import static com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler.multiHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.fail;

public abstract class ItestsBase6Windows extends ItestsBase5Unix {

    @Test
    @Assumption(methods = {"onWindows", "onlyCifsWinrm"})
    public void shouldThrowValidationMessageWhenTryingToConnectWithOldStyleWindowsDomainAccount() {
        assertWithOldStyleDomain("Cannot create a " + CIFS_PROTOCOL + ":" + WINRM_INTERNAL.toString().toLowerCase() + " connection with an old-style Windows domain account");
    }

    @Test
    @Assumption(methods = {"onWindows", "onlySmbWinrm"})
    public void shouldThrowValidationMessageWhenTryingToConnectWithOldStyleWindowsDomainAccountSmb() {
        assertWithOldStyleDomain("Cannot create a " + SMB_PROTOCOL + ":" + WINRM_INTERNAL.toString().toLowerCase() + " connection with an old-style Windows domain account");
    }

    @Test
    @Assumption(methods = {"onWindows", "onlyCifsTelnet"})
    public void shouldThrowValidationMessageWhenTryingToConnectWithNewStyleWindowsDomainAccount() {
        assertWithNewStyleDomain("Cannot create a " + CIFS_PROTOCOL + ":" + TELNET.toString().toLowerCase() + " connection with a new-style Windows domain account");
    }

    @Test
    @Assumption(methods = {"onWindows", "onlySmbTelnet"})
    public void shouldThrowValidationMessageWhenTryingToConnectWithNewStyleWindowsDomainAccountSmb() {
        assertWithNewStyleDomain("Cannot create a " + SMB_PROTOCOL + ":" + TELNET.toString().toLowerCase() + " connection with a new-style Windows domain account");
    }

    @Test
    @Assumption(methods = {"onWindows", "onlyLocal"})
    public void shouldListFilesOnWindowsLocalHost() throws IOException {

        File dir = temp.newFolder("overthere");
        Path file = Files.createFile(Paths.get(dir.getAbsolutePath(), "test.txt"));

        OverthereFile folder = connection.getFile(dir.getPath());
        List<OverthereFile> filesInFolder = folder.listFiles();

        OverthereFile expectedFile = connection.getFile(file.toAbsolutePath().toString());
        assertThat(filesInFolder.contains(expectedFile), equalTo(true));
    }

    @Test
    @Assumption(methods = {"onWindows", "notLocal"})
    public void shouldListFilesOnWindows() {
        OverthereFile folder = connection.getFile("C:\\overthere");
        List<OverthereFile> filesInFolder = folder.listFiles();

        OverthereFile expectedFile = connection.getFile("C:\\overthere\\temp");
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
    @Assumption(methods = {"onWindows"})
    public void shouldExecuteCommandWithSpecialCharactersOnWindows() {
        CapturingOverthereExecutionOutputHandler capturingHandler = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), capturingHandler), loggingErrorHandler(logger), CmdLine.build("echo","hello|<>&^ w\"orld"));
        assertThat(res, equalTo(0));
        assertThat(capturingHandler.getOutput(), containsString("hello|<>&^ w\"orld"));
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
    @Assumption(methods = {"onWindows"})
    public void shouldExecuteCommandWithArgumentOnWindows() {
        CapturingOverthereExecutionOutputHandler capturingHandler = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), capturingHandler), loggingErrorHandler(logger), CmdLine.build("ipconfig", "/all"));
        assertThat(res, equalTo(0));
        assertThat(capturingHandler.getOutput(), containsString("Windows IP Configuration"));
    }

    @Test
    @Assumption(methods = {"onWindows"})
    public void shouldExecuteBatchFileOnWindows() throws IOException {
        OverthereFile scriptToRun = connection.getTempFile("helloworld.bat");
        writeData(scriptToRun, ("@echo Hello World").getBytes("UTF-8"));

        CapturingOverthereExecutionOutputHandler capturingHandler = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), capturingHandler), loggingErrorHandler(logger), CmdLine.build(scriptToRun.getPath()));
        assertThat(res, equalTo(0));
        assertThat(capturingHandler.getOutput(), containsString("Hello World"));
    }

    @Test
    @Assumption(methods = {"onWindows"})
    public void shouldExecuteBatchFileWithArgumentsOnWindows() throws IOException {
        String content = "Hello from the file just uploaded";
        OverthereFile fileToType = connection.getTempFile("hello world.txt");
        writeData(fileToType, content.getBytes("UTF-8"));
        OverthereFile scriptToRun = connection.getTempFile("helloworld.bat");
        writeData(scriptToRun, ("@type %1").getBytes("UTF-8"));


        CapturingOverthereExecutionOutputHandler capturingHandler = capturingHandler();
        int res = connection.execute(multiHandler(loggingOutputHandler(logger), capturingHandler), loggingErrorHandler(logger), CmdLine.build(scriptToRun.getPath(), fileToType.getPath()));
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
        OverthereFile scriptToRun = connection.getTempFile("echo.ps1");
        writeData(scriptToRun, ("Write-Host \"Enter your name the prompt\"\n" +
                "$name = [Console]::In.ReadLine()\n" +
                "Write-Host \"Hi $name\"").getBytes("UTF-8"));

        OverthereProcess process = connection.startProcess(CmdLine.build("powershell.exe", "-ExecutionPolicy", "Unrestricted", "-File", scriptToRun.getPath()));
        try {
            OutputStream stdin = process.getStdin();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getStdout()));

            waitForPrompt(stdout, "name");

            String reply = "Vincent";
            enterPrompt(stdin, reply);

            String hi = waitForPrompt(stdout, "Hi");
            assertThat(hi, containsString("Vincent"));
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
    public void shouldNormalizeWindowsPathWithForwardSlashes() {
        OverthereFile file = connection.getFile("C:/Windows/System32");
        assertThat(file.getPath(), equalTo("C:\\Windows\\System32"));
    }

    private void assertWithOldStyleDomain(String str) {
        ConnectionOptions incorrectUserNameOptions = new ConnectionOptions(options);
        incorrectUserNameOptions.set(USERNAME, "DOMAIN\\user");
        try {
            Overthere.getConnection(protocol, incorrectUserNameOptions);
            fail("Expected not to be able to connect with an old-style Windows domain account");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString(str));
        }
    }

    private void assertWithNewStyleDomain(String str) {
        ConnectionOptions incorrectUserNameOptions = new ConnectionOptions(options);
        incorrectUserNameOptions.set(USERNAME, "user@DOMAIN");
        try {
            Overthere.getConnection(protocol, incorrectUserNameOptions);
            fail("Expected not to be able to connect with a new-style Windows domain account");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString(str));
        }
    }
}
