package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereExecutionOutputHandler;

public class NullOverthereExecutionOutputHandler implements OverthereExecutionOutputHandler {

    private NullOverthereExecutionOutputHandler() {
    }

    @Override
    public void handleChar(final char c) {
        // no-op
    }

    @Override
    public void handleLine(final String line) {
        // no-op
    }

    public static NullOverthereExecutionOutputHandler swallow() {
        return new NullOverthereExecutionOutputHandler();
    }
}
