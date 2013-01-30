package com.xebialabs.overthere.util;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.Lists;

import com.xebialabs.overthere.OverthereExecutionOutputHandler;

import static com.google.common.base.Joiner.on;
import static java.util.Collections.unmodifiableList;

/**
 * An implementation of the {@link com.xebialabs.overthere.OverthereExecutionOutputHandler} that captures the output in variables.
 */
public class CapturingOverthereExecutionOutputHandler implements OverthereExecutionOutputHandler {

    private List<String> outputLines = Collections.synchronizedList(Lists.<String>newArrayList());

    private CapturingOverthereExecutionOutputHandler() {
    }

    @Override
    public void handleChar(final char c) {
        // no-op
    }

    @Override
    public void handleLine(final String line) {
        outputLines.add(line);
    }

    /**
     * Returns the captured regular output lines.
     *
     * @return a list of captured regular output lines.
     */
    public List<String> getOutputLines() {
        return unmodifiableList(outputLines);
    }

    /**
     * Returns the captured regular output, with the lines separated by "\n".
     *
     * @return the captured regular output as one string.
     */
    public String getOutput() {
        return on('\n').join(outputLines);
    }

    /**
     * Creates a {@link CapturingOverthereExecutionOutputHandler}.
     *
     * @return the created {@link CapturingOverthereExecutionOutputHandler}.
     */
    public static CapturingOverthereExecutionOutputHandler capturingHandler() {
        return new CapturingOverthereExecutionOutputHandler();
    }

}
