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
package com.xebialabs.overthere.util;

import java.io.PrintStream;

import com.xebialabs.overthere.OverthereExecutionOutputHandler;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Implementation of the {@link com.xebialabs.overthere.OverthereExecutionOutputHandler} interface that prints the output to the console (either
 * {@link System#out} or {@link System#err}).
 */
public class ConsoleOverthereExecutionOutputHandler implements OverthereExecutionOutputHandler {

    private PrintStream stream;

    private ConsoleOverthereExecutionOutputHandler(final PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public void handleChar(final char c) {
        // no-op
    }

    @Override
    public void handleLine(final String line) {
        stream.println(line);
    }

    /**
     * Creates a {@link ConsoleOverthereExecutionOutputHandler} that logs to {@link System#out}.
     *
     * @return the created {@link ConsoleOverthereExecutionOutputHandler}.
     */
    public static ConsoleOverthereExecutionOutputHandler sysoutHandler() {
        return new ConsoleOverthereExecutionOutputHandler(out);
    }

    /**
     * Creates a {@link ConsoleOverthereExecutionOutputHandler} that logs to {@link System#err}.
     *
     * @return the created {@link ConsoleOverthereExecutionOutputHandler}.
     */
    public static ConsoleOverthereExecutionOutputHandler syserrHandler() {
        return new ConsoleOverthereExecutionOutputHandler(err);
    }
}
