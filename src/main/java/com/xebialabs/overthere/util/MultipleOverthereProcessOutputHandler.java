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
 * Implementation of the {@link OverthereProcessOutputHandler} interface that sends the output to one or more other
 * {@link OverthereProcessOutputHandler handlers}.
 *
 * @deprecated See {@link MultipleOverthereExecutionOutputHandler}
 */
@Deprecated
public class MultipleOverthereProcessOutputHandler implements OverthereProcessOutputHandler {

    private final OverthereProcessOutputHandler[] handlers;

    private MultipleOverthereProcessOutputHandler(final OverthereProcessOutputHandler... handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handleOutputLine(final String line) {
        for (OverthereProcessOutputHandler h : handlers) {
            h.handleOutputLine(line);
        }
    }

    @Override
    public void handleErrorLine(final String line) {
        for (OverthereProcessOutputHandler h : handlers) {
            h.handleErrorLine(line);
        }
    }

    @Override
    public void handleOutput(final char c) {
        for (OverthereProcessOutputHandler h : handlers) {
            h.handleOutput(c);
        }
    }

    /**
     * Creates a {@link MultipleOverthereProcessOutputHandler}.
     *
     * @param handlers the handlers where the output should be sent to.
     * @return the created {@link MultipleOverthereProcessOutputHandler}.
     * @deprecated Use {@link MultipleOverthereExecutionOutputHandler#multiHandler(com.xebialabs.overthere.OverthereExecutionOutputHandler...)}
     */
    @Deprecated
    public static MultipleOverthereProcessOutputHandler multiHandler(final OverthereProcessOutputHandler... handlers) {
        return new MultipleOverthereProcessOutputHandler(handlers);
    }

}
