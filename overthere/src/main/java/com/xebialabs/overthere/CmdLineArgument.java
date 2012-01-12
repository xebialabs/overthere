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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;

import java.io.Serializable;

/**
 * Represents a single command line argument.
 */
@SuppressWarnings("serial")
public abstract class CmdLineArgument implements Serializable {

	/**
	 * String containing special characters that require quoting or escaping on Unix.
	 */
	private static final String SPECIAL_CHARS_UNIX = " '\"\\;&|()${}*?";

	/**
	 * String containing special characters that require quoting or escaping on Windows.
	 */
	private static final String SPECIAL_CHARS_WINDOWS = " '\";&|()${}*?";

	/**
	 * String used to encode an empty argument as a string.
	 */
	private static final String EMPTY_ARGUMENT = "\"\"";

	/**
	 * Creates a regular argument.
	 * 
	 * @param arg
	 *            the argument string.
	 * @return the created argument.
	 */
	public static CmdLineArgument arg(String arg) {
		checkNotNull(arg, "Cannot create a null argument");
		return new Basic(arg);
	}

	/**
	 * Creates a password argument. When encoded for execution, a password argument is encoded like a regular argument. When encoded for logging, a password
	 * argument is always encoded as eight stars (********).
	 * 
	 * @param arg
	 *            the argument string.
	 * @return the created argument.
	 */
	public static CmdLineArgument password(String arg) {
		checkNotNull(arg, "Cannot create a null password argument");
		return new Password(arg);
	}

	/**
	 * Creates a raw argument. When encoded for execution or for logging, a raw argument is left as-is.
	 */
	public static CmdLineArgument raw(String arg) {
		checkNotNull(arg, "Cannot create a null password argument");
		return new Raw(arg);
	}
	
	/**
	 * Creates a nested command line argument. When encoded for execution or for logging, a nested command will be quoted. Useful for instance when executing su
	 * -c '<nestedcommand>'
	 * 
	 * @param line
	 *            the nested command line
	 * @return the created command
	 */
	public static CmdLineArgument nested(CmdLine line) {
		checkNotNull(line, "Cannot create a null nested command");
		return new Nested(line);
	}

	/**
	 * Returns a string representation of this argument.
	 * 
	 * @param os
	 *            the {@link OperatingSystemFamily operating system} to encode for.
	 * @param forLogging
	 *            <code>true</code> if this string representation will be used for logging.
	 * @return the string representation of this argument.
	 */
	public final String toString(OperatingSystemFamily os, boolean forLogging) {
		StringBuilder builder = new StringBuilder();
		buildString(os, forLogging, builder);
		return builder.toString();
	}

	/**
	 * Builds a string representation of this argument.
	 * 
	 * @param os
	 *            the {@link OperatingSystemFamily operating system} to encode for.
	 * @param forLogging
	 *            <code>true</code> if this string representation will be used for logging.
	 * @param builder
	 *            the {@link StringBuilder} to append to.
	 */
	public abstract void buildString(OperatingSystemFamily os, boolean forLogging, StringBuilder builder);

	/**
	 * Invokes <code>toString(UNIX, true)</code>.
	 */
	@Override
	public String toString() {
		return toString(UNIX, true);
	}

	protected void encodeString(String str, OperatingSystemFamily os, StringBuilder builder) {
		if (str.length() == 0) {
			builder.append(EMPTY_ARGUMENT);
			return;
		}

		switch (os) {
		case UNIX:
			if (!containsAny(str, SPECIAL_CHARS_UNIX)) {
				builder.append(str);
			} else {
				encodeArgumentWithSpecialCharactersForUnix(str, builder);
			}
			break;
		case WINDOWS:
			if (!containsAny(str, SPECIAL_CHARS_WINDOWS)) {
				builder.append(str);
			} else {
				encodeArgumentWithSpecialCharactersForWindows(str, builder);
			}
			break;
		default:
			throw new RuntimeException("Unknown os " + os);
		}
	}

	private boolean containsAny(String str, String chars) {
		for (char c : chars.toCharArray()) {
			if (str.indexOf(c) >= 0) {
				return true;
			}
		}
		return false;
	}

	private void encodeArgumentWithSpecialCharactersForWindows(String str, StringBuilder builder) {
		builder.append("\"");
		for (int j = 0; j < str.length(); j++) {
			char c = str.charAt(j);
			if (c == '\"') {
				builder.append(c);
			}
			builder.append(c);
		}
		builder.append("\"");
	}

	private void encodeArgumentWithSpecialCharactersForUnix(String str, StringBuilder builder) {
		for (int j = 0; j < str.length(); j++) {
			char c = str.charAt(j);
			if (SPECIAL_CHARS_UNIX.indexOf(c) != -1) {
				builder.append('\\');
			}
			builder.append(c);
		}
	}

	private abstract static class Single extends CmdLineArgument {
		protected String arg;

		private Single(String arg) {
			this.arg = arg;
		}
	}

	private static class Raw extends Single {

		public Raw(String arg) {
			super(arg);
		}

		@Override
		public void buildString(OperatingSystemFamily os, boolean forLogging, StringBuilder builder) {
			if (arg.length() == 0) {
				builder.append(EMPTY_ARGUMENT);
			} else {
				builder.append(arg);
			}
		}
	}

	private static class Basic extends Single {

		public Basic(String arg) {
			super(arg);
		}

		@Override
		public void buildString(OperatingSystemFamily os, boolean forLogging, StringBuilder builder) {
			String s = arg;
			encodeString(s, os, builder);
		}
	}

	private static class Password extends Basic {

		public Password(String arg) {
			super(arg);
		}

		@Override
		public void buildString(OperatingSystemFamily os, boolean forLogging, StringBuilder builder) {
			if (forLogging) {
				builder.append("********");
			} else {
				super.buildString(os, forLogging, builder);
			}
		}
	}

	private static class Nested extends CmdLineArgument {

		private final CmdLine line;

		public Nested(CmdLine line) {
			this.line = line;
		}

		@Override
		public void buildString(OperatingSystemFamily os, boolean forLogging, StringBuilder builder) {
			encodeString(line.toCommandLine(os, forLogging), os, builder);
		}
	}

}

