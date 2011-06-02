package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereProcessOutputHandler;

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

	public static MultipleOverthereProcessOutputHandler multiHandler(final OverthereProcessOutputHandler... handlers) {
		return new MultipleOverthereProcessOutputHandler(handlers);
	}

}
