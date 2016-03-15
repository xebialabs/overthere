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

import org.slf4j.Logger;

import com.xebialabs.overthere.OverthereProcessOutputHandler;

/**
 * Implementation of the {@link OverthereProcessOutputHandler} interface that sends the output to the specified logger.
 * Regular output will be logged at the INFO level, error output at the ERROR level.
 *
 * @deprecated See {@link LoggingOverthereExecutionOutputHandler}
 */
@Deprecated
public class LoggingOverthereProcessOutputHandler implements OverthereProcessOutputHandler {

    private final Logger logger;

    private LoggingOverthereProcessOutputHandler(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void handleOutputLine(String line) {
        logger.info(line);
    }

    @Override
    public void handleErrorLine(String line) {
        logger.error(line);
    }

    @Override
    public void handleOutput(char c) {
        // no-op
    }

    /**
     * Creates a {@link LoggingOverthereProcessOutputHandler}.
     *
     * @param logger the logger to send the output to.
     * @return the created {@link LoggingOverthereProcessOutputHandler}.
     * @deprecated Use {@link LoggingOverthereExecutionOutputHandler#loggingOutputHandler(org.slf4j.Logger)} or {@link LoggingOverthereExecutionOutputHandler#loggingErrorHandler(org.slf4j.Logger)}}.
     */
    @Deprecated
    public static LoggingOverthereProcessOutputHandler loggingHandler(final Logger logger) {
        return new LoggingOverthereProcessOutputHandler(logger);
    }

}
