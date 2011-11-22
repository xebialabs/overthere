package com.xebialabs.overthere;

import com.google.common.base.Function;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static com.xebialabs.overthere.CmdLineArgument.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a command line.
 */
@SuppressWarnings("serial")
public class CmdLine implements Serializable {

	List<CmdLineArgument> arguments = newArrayList();

	/**
	 * Adds {@link CmdLineArgument#arg(String) a regular argument} to the command line.
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
	 * Adds {@link CmdLineArgument#password(String) a password argument} to the command line.
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
	 * Adds {@link CmdLineArgument#raw(String) a raw argument} to the command line.
	 * 
	 * @param arg
	 *            the argument string to add.
	 * @return this.
	 */
	public CmdLine addRaw(String arg) {
		arguments.add(raw(arg));
		return this;
	}

	/**
	 * Adds {@link CmdLineArgument#nested(CmdLine) a nested command line} to the command line.
	 * 
	 * @param commandLine
	 *            the command line to add.
	 * @return this.
	 */
	public CmdLine addNested(CmdLine commandLine) {
		arguments.add(nested(commandLine));
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
	 * @param os
	 *            the operating system on which the result will be executed.
	 * @param forLogging
	 *            <code>true</code> if these string representations will be used for logging.
	 * @return an array with the string representations of the command line arguments.
	 */
	public String[] toCommandArray(final OperatingSystemFamily os, final boolean forLogging) {
		checkState(arguments.size() > 0, "Cannot encode empty command line");
		return transform(arguments, new Function<CmdLineArgument, String>() {
			@Override
			public String apply(CmdLineArgument from) {
				return from.toString(os, forLogging);
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
	public String toCommandLine(final OperatingSystemFamily os, final boolean forLogging) {
		checkState(arguments.size() > 0, "Cannot encode empty command line");
		StringBuilder sb = new StringBuilder();
		for (CmdLineArgument a : arguments) {
			if (sb.length() > 0) {
				sb.append(' ');
			}
			a.buildString(os, forLogging, sb);
		}

		return sb.toString();
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
