package com.xebialabs.overthere.util;

import org.slf4j.Logger;

import com.xebialabs.overthere.OverthereExecutionOutputHandler;

/**
 * Implementation of the {@link com.xebialabs.overthere.OverthereExecutionOutputHandler} interface that sends the output to the specified logger.
 * Regular output will be logged at the INFO level, error output at the ERROR level.
 *
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
     * @param logger
     *            the logger to send the output to.
     * @return the created {@link LoggingOverthereExecutionOutputHandler}.
     */
    public static LoggingOverthereExecutionOutputHandler loggingOutputHandler(Logger logger) {
        return new LoggingOverthereExecutionOutputHandler(logger, LogLevel.INFO);
    }

    /**
     * Creates a {@link LoggingOverthereExecutionOutputHandler} that logs on ERROR level.
     *
     * @param logger
     *            the logger to send the output to.
     * @return the created {@link LoggingOverthereExecutionOutputHandler}.
     */
    public static LoggingOverthereExecutionOutputHandler loggingErrorHandler(Logger logger) {
        return new LoggingOverthereExecutionOutputHandler(logger, LogLevel.ERROR);
    }
}
