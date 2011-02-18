package com.xebialabs.overthere.common;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CommandExecutionCallbackHandler;

/**
 * Runnable object that reads an {@link InputStream} an invokes a {@link CommandExecutionCallbackHandler} for every line read.
 */
public class OutputStreamToCallbackHandler implements Runnable {

	private InputStream in;

	private CommandExecutionCallbackHandler handler;

	public OutputStreamToCallbackHandler(InputStream in, CommandExecutionCallbackHandler handler) {
		this.in = checkNotNull(in, "InputStream is null");
		this.handler = checkNotNull(handler, "CommandExecutionCallbackHandler is null");
	}

	public void run() {

		InputStreamReader reader = new InputStreamReader(in);
		try {
			int readInt = reader.read();
			StringBuffer lineBuffer = new StringBuffer();
			while (readInt > -1) {
				char c = (char) readInt;
				handler.handleOutput(c);
				if (c != '\r' && c != '\n') {
					// add any character but a CR or LF to the line buffer
					lineBuffer.append(c);
				}
				if (c == '\n') {
					handler.handleOutputLine(lineBuffer.toString());
					lineBuffer = new StringBuffer();
				}
				readInt = reader.read();
			}
		} catch (Exception e) {
			logger.error("An exception occured", e);
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
	}

	private static Logger logger = LoggerFactory.getLogger(OutputStreamToCallbackHandler.class);

}