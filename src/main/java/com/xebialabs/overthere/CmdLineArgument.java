/*
 * Copyright (c) 2008-2013, XebiaLabs B.V., All rights reserved.
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

import static com.google.common.base.Preconditions.checkNotNull;

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
     * Creates a password argument. When encoded for execution, a password argument is encoded like a regular argument.
     * When encoded for logging, a password argument is always encoded as eight stars (********).
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
     * Creates a nested command line argument. When encoded for execution or for logging, a nested command will be
     * quoted. Useful for instance when executing su -c '<nestedcommand>'
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

    protected void encodeString(String str, OperatingSystemFamily os, StringBuilder builder) {
        if (str.length() == 0) {
            builder.append(EMPTY_ARGUMENT);
            return;
        }

        switch (os) {
        case WINDOWS:
            if (!containsAny(str, SPECIAL_CHARS_WINDOWS)) {
                builder.append(str);
            } else {
                encodeArgumentWithSpecialCharactersForWindows(str, builder);
            }
            break;
        case UNIX:
        case ZOS:
            if (!containsAny(str, SPECIAL_CHARS_UNIX)) {
                builder.append(str);
            } else {
                encodeArgumentWithSpecialCharactersForUnix(str, builder);
            }
            break;
        default:
            throw new RuntimeException("Unknown os " + os);
        }
    }

    private static boolean containsAny(String str, String chars) {
        for (char c : chars.toCharArray()) {
            if (str.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    private static void encodeArgumentWithSpecialCharactersForWindows(String str, StringBuilder builder) {
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

    private static void encodeArgumentWithSpecialCharactersForUnix(String str, StringBuilder builder) {
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

        @Override
        public String toString() {
            return arg;
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

        private static final String HIDDEN_PASSWORD = "********";

        public Password(String arg) {
            super(arg);
        }

        @Override
        public void buildString(OperatingSystemFamily os, boolean forLogging, StringBuilder builder) {
            if (forLogging) {
                builder.append(HIDDEN_PASSWORD);
            } else {
                super.buildString(os, forLogging, builder);
            }
        }

        @Override
        public String toString() {
            return HIDDEN_PASSWORD;
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

        @Override
        public String toString() {
            return line.toString();
        }
    }

}
