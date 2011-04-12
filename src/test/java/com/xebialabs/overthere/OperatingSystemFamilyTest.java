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

import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static org.apache.commons.lang.StringUtils.replace;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class OperatingSystemFamilyTest {

	private String command = "C:\\Program Files\\WebSphere\\bin\\wsadmin.bat";

	private String regularArgument = "aNormalValue";

	private String emptyArgument = "";

	private String argumentWithSpaces = "the argument with spaces";

	private String argumentWithSpecialChars = "heretheycome'\"\\;()${}andthatwasem";

	private String[] cmdarray = { command, regularArgument, emptyArgument, argumentWithSpaces, argumentWithSpecialChars };

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionOnNullCmdArray_encodeForExecutionOnWindows() {
		WINDOWS.encodeCommandLineForExecution((String[]) null);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionOnNullCmdArray_encodeForLoggingOnWindows() {
		WINDOWS.encodeCommandLineForLogging((String[]) null);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionOnNullCmdArray_encodeForExecutionOnUnix() {
		UNIX.encodeCommandLineForExecution((String[]) null);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionOnNullCmdArray_encodeForLoggingOnUnix() {
		UNIX.encodeCommandLineForLogging((String[]) null);
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionOnNullArgument_encodeForExecutionOnWindows() {
		WINDOWS.encodeCommandLineForExecution(new String[] { null });
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionOnNullArgument_encodeForLoggingOnWindows() {
		WINDOWS.encodeCommandLineForLogging(new String[] { null });
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionOnNullArgument_encodeForExecutionOnUnix() {
		UNIX.encodeCommandLineForExecution(new String[] { null });
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowNullPointerExceptionOnNullArgument_encodeForLoggingOnUnix() {
		UNIX.encodeCommandLineForLogging(new String[] { null });
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionForEmptyArray_encodeForExecutionOnWindows() {
		WINDOWS.encodeCommandLineForExecution(new String[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionForEmptyArray_encodeForLoggingOnWindows() {
		WINDOWS.encodeCommandLineForLogging(new String[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionForEmptyArray_encodeForExecutionOnUnix() {
		UNIX.encodeCommandLineForExecution(new String[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowIllegalArgumentExceptionForEmptyArray_encodeForLoggingOnUnix() {
		UNIX.encodeCommandLineForLogging(new String[0]);
	}

	@Test
	public void shouldHidePasswordWhenEncodingForLogging() {
		String[] cmdarray = { "wsadmin.sh", "-user", "admin", "-password", "secret" };
		String actualEncodedCommandLine = WINDOWS.encodeCommandLineForLogging(cmdarray);
		assertThat(actualEncodedCommandLine, equalTo("wsadmin.sh -user admin -password ********"));
	}

	@Test
	public void shouldEncodeCorrectlyForWindows() {
		String encodedCommand = "\"" + command + "\"";
		String encodedEmptyArgument = "\"\"";
		String encodedArgumentWithSpaces = "\"" + argumentWithSpaces + "\"";
		String encodedArgumentWithSpecialChars = "\"heretheycome'\"\"\\;()${}andthatwasem\"";

		String actualEncodedCommandLine = WINDOWS.encodeCommandLineForExecution(cmdarray);

		String[] encodedCmdArray = { encodedCommand, regularArgument, encodedEmptyArgument, encodedArgumentWithSpaces, encodedArgumentWithSpecialChars };
		String expectedEncodedCommandLine = StringUtils.join(encodedCmdArray, ' ');
		assertThat(actualEncodedCommandLine, equalTo(expectedEncodedCommandLine));
	}

	@Test
	public void shouldEncodeCorrectlyForUnix() {
		String encodedCommand = replace(replace(command, "\\", "\\\\"), " ", "\\ ");
		String encodedEmptyArgument = "\"\"";
		String encodedArgumentWithSpaces = replace(argumentWithSpaces, " ", "\\ ");
		String encodedArgumentWithSpecialChars = "heretheycome\\'\\\"\\\\\\;\\(\\)\\$\\{\\}andthatwasem";

		String actualEncodedCommandLine = UNIX.encodeCommandLineForExecution(cmdarray);

		String[] encodedCmdArray = { encodedCommand, regularArgument, encodedEmptyArgument, encodedArgumentWithSpaces, encodedArgumentWithSpecialChars };
		String expectedEncodedCommandLine = StringUtils.join(encodedCmdArray, ' ');
		assertThat(actualEncodedCommandLine, equalTo(expectedEncodedCommandLine));
	}

}

