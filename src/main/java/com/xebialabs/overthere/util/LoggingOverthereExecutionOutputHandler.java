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

import com.xebialabs.overthere.OverthereExecutionOutputHandler;

/**
 * Implementation of the {@link com.xebialabs.overthere.OverthereExecutionOutputHandler} interface that sends the output to the specified logger.
 * Regular output will be logged at the INFO level, error output at the ERROR level.
 */
public class LoggingOverthereExecutionOutputHandler implements OverthereExecutionOutputHandler {

    /**
     * Enum that controls to which level the message gets sent.
     */
    private static enum LogLevel {
        INFO {
            void log(Logger logger, String message) {
                logger.info(message);
            }
        },
        ERROR {
            void log(Logger logger, String message) {
                logger.error(message);
            }
        };

        abstract void log(Logger logger, String message);
    }

    private Logger logger;

    private LogLevel level;

    private LoggingOverthereExecutionOutputHandler(final Logger logger, final LogLevel level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void handleChar(final char c) {
        // no-op
    }

    @Override
    public void handleLine(final String line) {
        level.log(logger, line);
    }

    /**
     * Creates a {@link LoggingOverthereExecutionOutputHandler} that logs on INFO level.
     *
     * @param logger the logger to send the output to.
     * @return the created {@link LoggingOverthereExecutionOutputHandler}.
     */
    public static LoggingOverthereExecutionOutputHandler loggingOutputHandler(Logger logger) {
        return new LoggingOverthereExecutionOutputHandler(logger, LogLevel.INFO);
    }

    /**
     * Creates a {@link LoggingOverthereExecutionOutputHandler} that logs on ERROR level.
     *
     * @param logger the logger to send the output to.
     * @return the created {@link LoggingOverthereExecutionOutputHandler}.
     */
    public static LoggingOverthereExecutionOutputHandler loggingErrorHandler(Logger logger) {
        return new LoggingOverthereExecutionOutputHandler(logger, LogLevel.ERROR);
    }
}
