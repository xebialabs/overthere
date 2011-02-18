package com.xebialabs.overthere.common;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.LineIterator;

import com.xebialabs.overthere.CommandExecutionCallbackHandler;

/**
 * Runnable object that reads an {@link InputStream} an invokes a {@link CommandExecutionCallbackHandler} for every line read.
 */
public class ErrorStreamToCallbackHandler implements Runnable {

	private InputStream in;

	private CommandExecutionCallbackHandler handler;

	public ErrorStreamToCallbackHandler(InputStream in, CommandExecutionCallbackHandler handler) {
		this.in = in;
		this.handler = handler;
	}

	public void run() {
		if (handler == null) {
			return;
		}

		LineIterator lines = new LineIterator(new InputStreamReader(in));
		try {
			while (lines.hasNext()) {
				String line = lines.nextLine();
				handler.handleErrorLine(line);
			}
		} finally {
			LineIterator.closeQuietly(lines);
		}
	}

}
