package com.xebialabs.overthere.util;

import java.io.PrintStream;

import com.xebialabs.overthere.OverthereExecutionOutputHandler;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Implementation of the {@link com.xebialabs.overthere.OverthereExecutionOutputHandler} interface that prints the output to the console (either
 * {@link System#out} or {@link System#err}).
 */
public class ConsoleOverthereExecutionOutputHandler implements OverthereExecutionOutputHandler {

    private PrintStream stream;

    private ConsoleOverthereExecutionOutputHandler(final PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public void handleChar(final char c) {
        // no-op
    }

    @Override
    public void handleLine(final String line) {
        stream.println(line);
    }

    /**
     * Creates a {@link ConsoleOverthereExecutionOutputHandler} that logs to {@link System#out}.
     *
     * @return the created {@link ConsoleOverthereExecutionOutputHandler}.
     */
    public static ConsoleOverthereExecutionOutputHandler sysoutHandler() {
        return new ConsoleOverthereExecutionOutputHandler(out);
    }

    /**
     * Creates a {@link ConsoleOverthereExecutionOutputHandler} that logs to {@link System#err}.
     *
     * @return the created {@link ConsoleOverthereExecutionOutputHandler}.
     */
    public static ConsoleOverthereExecutionOutputHandler syserrHandler() {
        return new ConsoleOverthereExecutionOutputHandler(err);
    }
}
