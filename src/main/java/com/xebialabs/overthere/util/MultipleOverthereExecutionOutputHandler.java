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

import com.xebialabs.overthere.OverthereExecutionOutputHandler;

/**
 * Implementation of the {@link com.xebialabs.overthere.OverthereExecutionOutputHandler} interface that sends the output to one or more other
 * {@link com.xebialabs.overthere.OverthereProcessOutputHandler handlers}.
 */
public class MultipleOverthereExecutionOutputHandler implements OverthereExecutionOutputHandler {

    private final OverthereExecutionOutputHandler[] handlers;

    private MultipleOverthereExecutionOutputHandler(final OverthereExecutionOutputHandler... handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handleLine(final String line) {
        for (OverthereExecutionOutputHandler h : handlers) {
            h.handleLine(line);
        }
    }

    @Override
    public void handleChar(final char c) {
        for (OverthereExecutionOutputHandler h : handlers) {
            h.handleChar(c);
        }
    }

    /**
     * Creates a {@link com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler}.
     *
     * @param handlers the handlers where the output should be sent to.
     * @return the created {@link com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler}.
     */
    public static MultipleOverthereExecutionOutputHandler multiHandler(final OverthereExecutionOutputHandler... handlers) {
        return new MultipleOverthereExecutionOutputHandler(handlers);
    }
}
