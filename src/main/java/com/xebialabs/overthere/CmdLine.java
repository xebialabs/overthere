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
	private static final String SPECIAL_CHARS = " '\"\\;()${}";

	List<CmdLineArgument> arguments = newArrayList();

	public CmdLine addArgument(String arg) {
		arguments.add(arg(arg));
		return this;
	}

	public CmdLine addPassword(String arg) {
		arguments.add(password(arg));
		return this;
	}

	public CmdLine add(CmdLineArgument arg) {
		checkNotNull(arg, "Cannot add null CmdLineArgument");
		arguments.add(arg);
		return this;
	}

	public CmdLine add(List<CmdLineArgument> args) {
		checkNotNull(args, "Cannot add null List<CmdLineArgument>");
		arguments.addAll(args);
		return this;
	}

	public List<CmdLineArgument> getArguments() {
		return unmodifiableList(arguments);
	}

	public String[] toCommandArray(final boolean forLogging) {
		checkState(arguments.size() > 0, "Cannot encode empty command line");
		return transform(arguments, new Function<CmdLineArgument, String>() {
			@Override
			public String apply(CmdLineArgument from) {
				return from.toString(forLogging);
			}
		}).toArray(new String[arguments.size()]);
	}

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
		for(char c : chars.toCharArray()) {
			if(str.indexOf(c) >= 0) {
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

	public String toString() {
		return toCommandLine(UNIX, true);
	}

	public static CmdLine build(String... cmdarray) {
		CmdLine cmdLine = new CmdLine();
		for (String s : cmdarray) {
			cmdLine.addArgument(s);
		}
		return cmdLine;
	}

}
