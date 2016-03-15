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

import com.xebialabs.overthere.OverthereProcessOutputHandler;

/**
 * Implementation of the {@link OverthereProcessOutputHandler} interface that prints the output to the console (
 * {@link System#out} and {@link System#err}).
 *
 * @deprecated See {@link ConsoleOverthereExecutionOutputHandler}
 */
@Deprecated
public class ConsoleOverthereProcessOutputHandler implements OverthereProcessOutputHandler {

    private ConsoleOverthereProcessOutputHandler() {
    }

    @Override
    public void handleOutputLine(final String line) {
        System.out.println(line);
    }

    @Override
    public void handleErrorLine(final String line) {
        System.err.println(line);
    }

    @Override
    public void handleOutput(final char c) {
        // no-op
    }

    /**
     * Creates a {@link ConsoleOverthereProcessOutputHandler}.
     *
     * @return the created {@link ConsoleOverthereProcessOutputHandler}.
     */
    public static ConsoleOverthereProcessOutputHandler consoleHandler() {
        return new ConsoleOverthereProcessOutputHandler();
    }

}
