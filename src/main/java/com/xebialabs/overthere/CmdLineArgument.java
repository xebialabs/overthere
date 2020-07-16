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

import com.xebialabs.overthere.util.UnixCommandLineArgsSanitizer;
import com.xebialabs.overthere.util.WinRSCommandLinePasswordSanitizer;
import com.xebialabs.overthere.util.WindowsCommandLineArgsSanitizer;

import java.io.Serializable;

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;

/**
 * Represents a single command line argument.
 */
@SuppressWarnings("serial")
public abstract class CmdLineArgument implements Serializable {

    /**
     * String used to encode an empty argument as a string.
     */
    private static final String EMPTY_ARGUMENT = "\"\"";

    /**
     * Creates a regular argument.
     *
     * @param arg the argument string.
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
     * @param arg the argument string.
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
        checkNotNull(arg, "Cannot create a null raw argument");
        return new Raw(arg);
    }

    /**
     * Creates a nested command line argument. When encoded for execution or for logging, a nested command will be
     * quoted. Useful for instance when executing su -c '<nestedcommand>'
     *
     * @param line the nested command line
     * @return the created command
     */
    public static CmdLineArgument nested(CmdLine line) {
        checkNotNull(line, "Cannot create a null nested command");
        return new Nested(line);
    }

    /**
     * Returns a string representation of this argument.
     *
     * @param os         the {@link OperatingSystemFamily operating system} to encode for.
     * @param forLogging <code>true</code> if this string representation will be used for logging.
     * @return the string representation of this argument.
     */
    public abstract String toString(OperatingSystemFamily os, boolean forLogging);

    /**
     * Builds a string representation of this argument.
     *
     * @param os         the {@link OperatingSystemFamily operating system} to encode for.
     * @param forLogging <code>true</code> if this string representation will be used for logging.
     * @param builder    the {@link StringBuilder} to append to.
     */
    public abstract void buildString(OperatingSystemFamily os, boolean forLogging, StringBuilder builder);

    protected void encodeString(String str, OperatingSystemFamily os, StringBuilder builder) {
        if (str.length() == 0) {
            builder.append(EMPTY_ARGUMENT);
            return;
        }

        switch (os) {
            case WINDOWS:
                if (!WindowsCommandLineArgsSanitizer.containsAnySpecialChars(str)) {
                    builder.append(str);
                } else {
                    builder.append(WindowsCommandLineArgsSanitizer.sanitize(str));
                }
                break;
            case UNIX:
            case ZOS:
                if (!UnixCommandLineArgsSanitizer.containsAnySpecialChars(str)) {
                    builder.append(str);
                } else {
                    builder.append(UnixCommandLineArgsSanitizer.sanitize(str));
                }
                break;
            default:
                throw new RuntimeException("Unknown os " + os);
        }
    }

    private abstract static class Single extends CmdLineArgument {
        protected String arg;

        private Single(String arg) {
            this.arg = arg;
        }

        @Override
        public String toString() {
            return toString(UNIX, true);
        }
    }

    private static class Raw extends Single {

        public Raw(String arg) {
            super(arg);
        }

        @Override
        public String toString(OperatingSystemFamily os, boolean forLogging) {
            return arg;
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
        public String toString(OperatingSystemFamily os, boolean forLogging) {
            return arg;
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
        public String toString(OperatingSystemFamily os, boolean forLogging) {
            if(forLogging) {
                return HIDDEN_PASSWORD;
            } else {
                return arg;
            }
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
        protected void encodeString(String str, OperatingSystemFamily os, StringBuilder builder) {
            if (str.length() == 0) {
                builder.append(EMPTY_ARGUMENT);
                return;
            }

            switch (os) {
                case WINDOWS:
                    if (!WinRSCommandLinePasswordSanitizer.containsAnySpecialChars(str)) {
                        builder.append(str);
                    } else {
                        builder.append(WinRSCommandLinePasswordSanitizer.sanitize(str));
                    }
                    break;
                case UNIX:
                case ZOS:
                    super.encodeString(str, os, builder);
                    break;
                default:
                    throw new RuntimeException("Unknown os " + os);
            }
        }
    }

    private static class Nested extends CmdLineArgument {

        private final CmdLine line;

        public Nested(CmdLine line) {
            this.line = line;
        }

        public String toString(OperatingSystemFamily os, boolean forLogging) {
            StringBuilder builder = new StringBuilder();
            encodeString(line.toCommandLine(os, forLogging), os, builder);
            return builder.toString();
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
