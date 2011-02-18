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

package com.xebialabs.overthere;

import static com.xebialabs.deployit.ci.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.deployit.ci.OperatingSystemFamily.UNIX;
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
		String[] cmdarray = { "wsadmin.sh", "-user", "admin", "-password", "secret"};
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

	// @Test
	// public void testMySqlCommandWithSpaces() {
	// final String expression = "--socket=/tmp/truck -u scott -p tiger COUNTRY";
	// assertEquals(expression, Encoder.encodeCommandLine(false, expression));
	// assertEquals(expression, Encoder.encodeCommandLine(true, expression));
	// }
	//
	// @Test
	// public void testSplittedMySqlCommandWithPassword() {
	// final String expression = "-username benoit -password deployit";
	// assertEquals(expression, Encoder.encodeCommandLine(false, "-username", "benoit", "-password", "deployit"));
	// assertEquals("-username benoit -password ********", Encoder.encodeCommandLine(true, "-username", "benoit", "-password", "deployit"));
	// }
	//
	// @Test
	// public void testMySqlCommandWithPassword() {
	// final String expression = "-username benoit -password deployit";
	// assertEquals(expression, Encoder.encodeCommandLine(false, expression));
	// assertEquals("-username benoit -password ********", Encoder.encodeCommandLine(true, expression));
	// }
}
