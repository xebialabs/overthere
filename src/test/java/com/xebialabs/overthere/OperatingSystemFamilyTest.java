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
