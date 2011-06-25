package com.xebialabs.overthere;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.xebialabs.overthere.CmdLineArgument.arg;
import static com.xebialabs.overthere.CmdLineArgument.password;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static java.util.Collections.unmodifiableList;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Function;

/**
 * Represents a command line.
 */
@SuppressWarnings("serial")
public class CmdLine implements Serializable {

	/**
	 * String containing special characters that require quoting or escaping.
	 */
	private static final String SPECIAL_CHARS = " '\"\\;()${}*?";

	List<CmdLineArgument> arguments = newArrayList();

	/**
	 * Adds a regular argument to the command line. A regular argument is an argument that is intended to be passed to the program that is invoked.
	 * 
	 * @param arg
	 *            the argument string to add.
	 * @return this.
	 */
	public CmdLine addArgument(String arg) {
		arguments.add(arg(arg));
		return this;
	}

	/**
	 * Adds a password argument to the command line. A password argument is an argument that is intended to be passed to the program that is invoked and that
	 * should not be shown in logs.
	 * 
	 * @param arg
	 *            the argument string to add.
	 * @return this.
	 */
	public CmdLine addPassword(String arg) {
		arguments.add(password(arg));
		return this;
	}

	/**
	 * Adds an {@link CmdLineArgument argument}.
	 * 
	 * @param arg
	 *            the argument to add.
	 * @return this.
	 */
	public CmdLine add(CmdLineArgument arg) {
		checkNotNull(arg, "Cannot add null CmdLineArgument");
		arguments.add(arg);
		return this;
	}

	/**
	 * Adds a list of {@link CmdLineArgument arguments}.
	 * 
	 * @param args
	 *            the arguments to add.
	 * @return this.
	 */
	public CmdLine add(List<CmdLineArgument> args) {
		checkNotNull(args, "Cannot add null List<CmdLineArgument>");
		arguments.addAll(args);
		return this;
	}

	/**
	 * Returns the argument on this command line.
	 * 
	 * @return the list of {@link CmdLineArgument arguments}.
	 */
	public List<CmdLineArgument> getArguments() {
		return unmodifiableList(arguments);
	}

	/**
	 * Converts this command line to a string array. All arguments are {@link CmdLineArgument#toString(boolean) converted to their string representation} and
	 * then returned as an array.
	 * 
	 * @param forLogging
	 *            <code>true</code> if these string representations will be used for logging.
	 * @return an array with the string representations of the command line arguments.
	 */
	public String[] toCommandArray(final boolean forLogging) {
		checkState(arguments.size() > 0, "Cannot encode empty command line");
		return transform(arguments, new Function<CmdLineArgument, String>() {
			@Override
			public String apply(CmdLineArgument from) {
				return from.toString(forLogging);
			}
		}).toArray(new String[arguments.size()]);
	}

	/**
	 * Converts this command line to a single String for execution on (or logging to) a specific target operating system.
	 * 
	 * @param os
	 *            the operating system on which the result will be executed.
	 * @param forLogging
	 *            <code>true</code> if the created command line will be used for logging.
	 * @return the command line as a single string
	 */
	public String toCommandLine(OperatingSystemFamily os, final boolean forLogging) {
		checkState(arguments.size() > 0, "Cannot encode empty command line");
		StringBuilder sb = new StringBuilder();
		for (CmdLineArgument a : arguments) {
			if (sb.length() > 0) {
				sb.append(' ');
			}

			if (forLogging && a.isPassword()) {
				encodePasswordArgument(a.toString(), sb);
			} else {
				encodeArgument(os, a.toString(forLogging), sb);
			}
		}

		return sb.toString();
	}

	private void encodePasswordArgument(String argument, StringBuilder collector) {
		collector.append("********");
	}

	private void encodeArgument(OperatingSystemFamily os, String argument, StringBuilder collector) {
		if (argument.length() == 0) {
			encodeEmptyArgument(collector);
		} else if (!containsAny(argument, SPECIAL_CHARS)) {
			encodeRegularArgument(argument, collector);
		} else {
			if (os == WINDOWS) {
				encodeArgumentWithSpecialCharactersForWindows(argument, collector);
			} else {
				encodeArgumentWithSpecialCharactersForNonWindows(argument, collector);
			}
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

	private void encodeEmptyArgument(StringBuilder collector) {
		collector.append("\"\"");
	}

	private void encodeRegularArgument(String argument, StringBuilder collector) {
		collector.append(argument);
	}

	private void encodeArgumentWithSpecialCharactersForWindows(String argument, StringBuilder collector) {
		collector.append("\"");
		for (int j = 0; j < argument.length(); j++) {
			char c = argument.charAt(j);
			if (c == '\"') {
				collector.append(c);
			}
			collector.append(c);
		}
		collector.append("\"");
	}

	private void encodeArgumentWithSpecialCharactersForNonWindows(String argument, StringBuilder collector) {
		for (int j = 0; j < argument.length(); j++) {
			char c = argument.charAt(j);
			if (SPECIAL_CHARS.indexOf(c) != -1) {
				collector.append('\\');
			}
			collector.append(c);
		}
	}

	/**
	 * Returns a string representation of this command line. All passwords are hidden.
	 */
	public String toString() {
		return toCommandLine(UNIX, true);
	}

	/**
	 * Builds a simple command line from one or more strings. For each string, a regular argument is created.
	 * 
	 * @param args
	 *            the regular arguments
	 * @return the created command line
	 */
	public static CmdLine build(String... args) {
		CmdLine cmdLine = new CmdLine();
		for (String s : args) {
			cmdLine.addArgument(s);
		}
		return cmdLine;
	}

}
