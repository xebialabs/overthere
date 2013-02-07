package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereExecutionOutputHandler;
import com.xebialabs.overthere.OverthereProcessOutputHandler;

@SuppressWarnings("deprecation")
public class OverthereProcessOutputHandlerWrapper implements OverthereExecutionOutputHandler {

    private OverthereProcessOutputHandler handler;

    private boolean stdout;

    /**
     *
     * @param handler The handler to wrap.
     * @param stdout Whether the wrapper is created for stdout (true), or stderr (false).
     */
    public OverthereProcessOutputHandlerWrapper(final OverthereProcessOutputHandler handler, boolean stdout) {
        this.handler = handler;
        this.stdout = stdout;
    }

    @Override
    public void handleChar(final char c) {
        if (stdout) {
            handler.handleOutput(c);
        }
    }

    @Override
    public void handleLine(final String line) {
        if (stdout) {
            handler.handleOutputLine(line);
        } else {
            handler.handleErrorLine(line);
        }
    }

    public static OverthereExecutionOutputHandler wrapStdout(OverthereProcessOutputHandler handler) {
        return new OverthereProcessOutputHandlerWrapper(handler, true);
    }

    public static OverthereExecutionOutputHandler wrapStderr(OverthereProcessOutputHandler handler) {
        return new OverthereProcessOutputHandlerWrapper(handler, false);
    }
}
