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
package com.xebialabs.overthere;

import java.util.List;
import org.testng.annotations.Test;

import static com.google.common.base.Joiner.on;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.OperatingSystemFamily.ZOS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class CmdLineTest {

    private String command = "C:\\Program Files\\WebSphere\\bin\\wsadmin.bat";

    private String regularArgument = "aNormalValue";

    private String emptyArgument = "";

    private String argumentWithSpaces = "the argument with spaces";

    private String argumentWithSpecialChars = "heretheycome'\"\\;()%${}*?andthatwasem";

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenAddingNullArgument() {
        new CmdLine().add((CmdLineArgument) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNullPointerExceptionWhenAddingNullArguments() {
        new CmdLine().add((List<CmdLineArgument>) null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionWhenEncodingEmptyCmdLineAsArray() {
        CmdLine commandLine = new CmdLine();
        commandLine.toCommandArray(UNIX, false);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void shouldThrowIllegalStateExceptionWhenEncodingEmptyCmdLineAsString() {
        CmdLine commandLine = new CmdLine();
        commandLine.toCommandLine(UNIX, false);
    }

    @Test
    public void shouldHidePasswordWhenEncodingForLogging() {
        CmdLine commandLine = new CmdLine().addArgument("wsadmin.sh").addArgument("-user").addArgument("admin").addArgument("-password").addPassword("secret");
        String actualEncodedCommandLine = commandLine.toCommandLine(WINDOWS, true);
        assertThat(actualEncodedCommandLine, equalTo("wsadmin.sh -user admin -password ********"));
    }

    @Test
    public void shouldEncodeCorrectlyForWindows() {
        CmdLine commandLine = CmdLine.build(command, regularArgument, emptyArgument, argumentWithSpaces, argumentWithSpecialChars);
        String actualEncodedCommandLine = commandLine.toCommandLine(WINDOWS, false);

        String encodedCommand = "\"" + command + "\"";
        String encodedEmptyArgument = "\"\"";
        String encodedArgumentWithSpaces = "\"" + argumentWithSpaces + "\"";
        String encodedArgumentWithSpecialChars = "heretheycome'\"\\;()%${}*?andthatwasem";
        String[] encodedCmdArray = {encodedCommand, regularArgument, encodedEmptyArgument, encodedArgumentWithSpaces, encodedArgumentWithSpecialChars};
        String expectedEncodedCommandLine = on(' ').join(encodedCmdArray);

        assertThat(actualEncodedCommandLine, equalTo(expectedEncodedCommandLine));
    }

    @Test
    public void shouldEncodeCorrectlyForUnix() {
        CmdLine commandLine = CmdLine.build(command, regularArgument, emptyArgument, argumentWithSpaces, argumentWithSpecialChars);
        String actualEncodedCommandLine = commandLine.toCommandLine(UNIX, false);

        String encodedCommand = command.replace("\\", "\\\\").replace(" ", "\\ ");
        String encodedEmptyArgument = "\"\"";
        String encodedArgumentWithSpaces = argumentWithSpaces.replace(" ", "\\ ");
        String encodedArgumentWithSpecialChars = "heretheycome\\'\\\"\\\\\\;\\(\\)%\\$\\{\\}\\*\\?andthatwasem";
        String[] encodedCmdArray = {encodedCommand, regularArgument, encodedEmptyArgument, encodedArgumentWithSpaces, encodedArgumentWithSpecialChars};
        String expectedEncodedCommandLine = on(' ').join(encodedCmdArray);

        assertThat(actualEncodedCommandLine, equalTo(expectedEncodedCommandLine));
    }

    @Test
    public void shouldEncodeCorrectlyForZos() {
        CmdLine commandLine = CmdLine.build(command, regularArgument, emptyArgument, argumentWithSpaces, argumentWithSpecialChars);
        String actualEncodedCommandLine = commandLine.toCommandLine(ZOS, false);

        String encodedCommand = command.replace("\\", "\\\\").replace(" ", "\\ ");
        String encodedEmptyArgument = "\"\"";
        String encodedArgumentWithSpaces = argumentWithSpaces.replace(" ", "\\ ");
        String encodedArgumentWithSpecialChars = "heretheycome\\'\\\"\\\\\\;\\(\\)%\\$\\{\\}\\*\\?andthatwasem";
        String[] encodedCmdArray = {encodedCommand, regularArgument, encodedEmptyArgument, encodedArgumentWithSpaces, encodedArgumentWithSpecialChars};
        String expectedEncodedCommandLine = on(' ').join(encodedCmdArray);

        assertThat(actualEncodedCommandLine, equalTo(expectedEncodedCommandLine));
    }

    @Test
    public void shouldLeaveRawArgumentAsIs() {
        CmdLine commandLine = new CmdLine().addArgument("rm").addArgument("-rf").addRaw("*");
        String actualEncodedCommandLine = commandLine.toCommandLine(UNIX, false);
        assertThat(actualEncodedCommandLine, equalTo("rm -rf *"));
    }

    @Test
    public void shouldEncodeNestedArgument() {
        CmdLine nestedCommandLine = new CmdLine().addArgument("rm").addPassword("a file");
        assertThat(nestedCommandLine.toCommandLine(UNIX, false), equalTo("rm a\\ file"));

        CmdLine commandLine = new CmdLine().addArgument("sudo").addNested(nestedCommandLine);
        assertThat(commandLine.toCommandLine(UNIX, false), equalTo("sudo rm\\ a\\\\\\ file"));
    }

    @Test
    public void shouldEncodeNestedPassword() {
        CmdLine nestedCommandLine = new CmdLine().addArgument("login").addPassword("secret");
        CmdLine commandLine = new CmdLine().addArgument("wrap").addNested(nestedCommandLine);
        assertThat(commandLine.toCommandLine(UNIX, false), equalTo("wrap login\\ secret"));
        assertThat(commandLine.toCommandLine(UNIX, true), equalTo("wrap login\\ \\*\\*\\*\\*\\*\\*\\*\\*"));
    }

    @Test
    public void shouldNotEncodeOnlyWhenToCommandLineIsCalled(){
        CmdLine cmdLine = new CmdLine();
        cmdLine.addArgument("-password:" + "P@ssword+-&");
        assertThat(cmdLine.getArguments().get(0).toString(WINDOWS, false), equalTo("-password:P@ssword+-&"));
        assertThat(cmdLine.toCommandLine(WINDOWS,false), equalTo("-password:P@ssword+-^&"));
    }

    @Test
    public void shouldEncodeNestedRaw() {
        CmdLine nestedCommandLine = new CmdLine().addArgument("rm").addRaw("*");
        assertThat(nestedCommandLine.toCommandLine(UNIX, false), equalTo("rm *"));

        CmdLine commandLine = new CmdLine().addArgument("sudo").addNested(nestedCommandLine);
        assertThat(commandLine.toCommandLine(UNIX, false), equalTo("sudo rm\\ \\*"));
    }

}
