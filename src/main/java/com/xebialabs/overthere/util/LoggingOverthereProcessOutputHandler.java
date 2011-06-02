package com.xebialabs.overthere.util;

import org.slf4j.Logger;

import com.xebialabs.overthere.OverthereProcessOutputHandler;

public class LoggingOverthereProcessOutputHandler implements OverthereProcessOutputHandler {

	private final Logger logger;

	public LoggingOverthereProcessOutputHandler(final Logger logger) {
		this.logger = logger;
	}

	@Override
	public void handleOutputLine(String line) {
		logger.debug("STDOUT: " + line);
	}

	@Override
	public void handleErrorLine(String line) {
		logger.debug("STDERR: " + line);
	}

	@Override
	public void handleOutput(char c) {
		// no-op
	}

	public static LoggingOverthereProcessOutputHandler loggingHandler(final Logger logger) {
		return new LoggingOverthereProcessOutputHandler(logger);
	}

}
