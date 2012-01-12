/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
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
		commandLine.toCommandArray(UNIX, false);
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
	public void shouldEncodeNestedRaw() {
		CmdLine nestedCommandLine = new CmdLine().addArgument("rm").addRaw("*");
		assertThat(nestedCommandLine.toCommandLine(UNIX, false), equalTo("rm *"));

		CmdLine commandLine = new CmdLine().addArgument("sudo").addNested(nestedCommandLine);
		assertThat(commandLine.toCommandLine(UNIX, false), equalTo("sudo rm\\ \\*"));
	}

}

