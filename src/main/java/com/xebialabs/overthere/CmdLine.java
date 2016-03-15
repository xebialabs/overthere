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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;
import static com.xebialabs.overthere.util.OverthereUtils.checkState;
import static com.xebialabs.overthere.CmdLineArgument.arg;
import static com.xebialabs.overthere.CmdLineArgument.nested;
import static com.xebialabs.overthere.CmdLineArgument.password;
import static com.xebialabs.overthere.CmdLineArgument.raw;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a command line.
 */
@SuppressWarnings("serial")
public class CmdLine implements Serializable {

    List<CmdLineArgument> arguments = new ArrayList<CmdLineArgument>();

    /**
     * Adds {@link CmdLineArgument#arg(String) a regular argument} to the command line.
     *
     * @param arg the argument string to add.
     * @return this.
     */
    public CmdLine addArgument(String arg) {
        arguments.add(arg(arg));
        return this;
    }

    /**
     * Adds {@link CmdLineArgument#password(String) a password argument} to the command line.
     *
     * @param arg the argument string to add.
     * @return this.
     */
    public CmdLine addPassword(String arg) {
        arguments.add(password(arg));
        return this;
    }

    /**
     * Adds {@link CmdLineArgument#raw(String) a raw argument} to the command line.
     *
     * @param arg the argument string to add.
     * @return this.
     */
    public CmdLine addRaw(String arg) {
        arguments.add(raw(arg));
        return this;
    }

    /**
     * Adds a {@link MessageFormat} compatible templated fragment to the command line as a series of {@link CmdLineArgument#arg(String) regular arguments}.
     *
     * @param template  The {@link MessageFormat} compatible templated fragment.
     * @param variables The variables that are substituted in the template.
     * @return this.
     */
    public CmdLine addTemplatedFragment(String template, Object... variables) {
        for (String arg : template.split("\\s+")) {
            if(arg.matches(".*\\{\\d+}\\.*")) {
                String fragment = MessageFormat.format(arg, variables);
                addArgument(fragment);
            } else {
                addRaw(arg);
            }
        }
        return this;
    }


    /**
     * Adds {@link CmdLineArgument#nested(CmdLine) a nested command line} to the command line.
     *
     * @param commandLine the command line to add.
     * @return this.
     */
    public CmdLine addNested(CmdLine commandLine) {
        arguments.add(nested(commandLine));
        return this;
    }

    /**
     * Adds an {@link CmdLineArgument argument}.
     *
     * @param arg the argument to add.
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
     * @param args the arguments to add.
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
     * Converts this command line to a string array. All arguments are
     * {@link CmdLineArgument#toString(OperatingSystemFamily, boolean) converted to their string representation} and
     * then returned as an array.
     *
     * @param os         the operating system on which the result will be executed.
     * @param forLogging <code>true</code> if these string representations will be used for logging.
     * @return an array with the string representations of the command line arguments.
     */
    public String[] toCommandArray(final OperatingSystemFamily os, final boolean forLogging) {
        checkState(arguments.size() > 0, "Cannot encode empty command line");
        String[] args = new String[arguments.size()];
        for (int i = 0; i < arguments.size(); i++) {
            args[i] = arguments.get(i).toString(os, forLogging);

        }
        return args;
    }

    /**
     * Converts this command line to a single String for execution on (or logging to) a specific target operating
     * system.
     *
     * @param os         the operating system on which the result will be executed.
     * @param forLogging <code>true</code> if the created command line will be used for logging.
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
    @Override
    public String toString() {
        return toCommandLine(UNIX, true);
    }

    /**
     * Builds a simple command line from one or more strings. For each string, a regular argument is created.
     *
     * @param args the regular arguments
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
