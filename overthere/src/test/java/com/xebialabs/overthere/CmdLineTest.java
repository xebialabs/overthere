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
package com.xebialabs.overthere;

import static com.google.common.base.Joiner.on;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

public class CmdLineTest {

	private String command = "C:\\Program Files\\WebSphere\\bin\\wsadmin.bat";

	private String regularArgument = "aNormalValue";

	private String emptyArgument = "";

	private String argumentWithSpaces = "the argument with spaces";

	private String argumentWithSpecialChars = "heretheycome'\"\\;()${}*?andthatwasem";

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionWhenAddingNullArgument() {
		new CmdLine().add((CmdLineArgument) null);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionWhenAddingNullArguments() {
		new CmdLine().add((List<CmdLineArgument>) null);
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowIllegalStateExceptionWhenEncodingEmptyCmdLineAsArray() {
		CmdLine commandLine = new CmdLine();
		commandLine.toCommandArray(false);
	}

	@Test(expected = IllegalStateException.class)
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
		String encodedArgumentWithSpecialChars = "\"heretheycome'\"\"\\;()${}*?andthatwasem\"";
		String[] encodedCmdArray = { encodedCommand, regularArgument, encodedEmptyArgument, encodedArgumentWithSpaces, encodedArgumentWithSpecialChars };
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
		String encodedArgumentWithSpecialChars = "heretheycome\\'\\\"\\\\\\;\\(\\)\\$\\{\\}\\*\\?andthatwasem";
		String[] encodedCmdArray = { encodedCommand, regularArgument, encodedEmptyArgument, encodedArgumentWithSpaces, encodedArgumentWithSpecialChars };
		String expectedEncodedCommandLine = on(' ').join(encodedCmdArray);

		assertThat(actualEncodedCommandLine, equalTo(expectedEncodedCommandLine));
	}

	@Test
	public void shouldLeaveRawArgumentAsIs() {
		CmdLine commandLine = new CmdLine().addArgument("rm").addArgument("-rf").addRaw("*");
		String actualEncodedCommandLine = commandLine.toCommandLine(UNIX, false);
		assertThat(actualEncodedCommandLine, equalTo("rm -rf *"));
	}

}
