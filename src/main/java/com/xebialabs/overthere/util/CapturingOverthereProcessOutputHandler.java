/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.util;

import java.util.ArrayList;
import java.util.List;

import com.xebialabs.overthere.OverthereProcessOutputHandler;

import static com.google.common.base.Joiner.on;
import static java.util.Collections.unmodifiableList;

/**
 * An implementation of the {@link OverthereProcessOutputHandler} that captures the output in variables.
 */
public final class CapturingOverthereProcessOutputHandler implements OverthereProcessOutputHandler {

    private final List<String> outputLines = new ArrayList<String>();

    private final List<String> errorLines = new ArrayList<String>();

    private final List<String> allLines = new ArrayList<String>();

    private CapturingOverthereProcessOutputHandler() {
    }

    @Override
    public void handleOutputLine(final String line) {
        outputLines.add(line);
        allLines.add(line);
    }

    @Override
    public void handleErrorLine(final String line) {
        errorLines.add(line);
        allLines.add(line);
    }

    @Override
    public void handleOutput(final char c) {
        // no-op
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
     * Returns the captured error output lines.
     * 
     * @return a list of captured error output lines.
     */
    public List<String> getErrorLines() {
        return unmodifiableList(errorLines);
    }

    /**
     * Returns the captured error output, with the lines separated by "\n".
     * 
     * @return the captured error output as one string.
     */
    public String getError() {
        return on('\n').join(errorLines);
    }

    /**
     * Returns the captured regular and error output lines.
     * 
     * @return a list of captured error regular and output lines.
     */
    public List<String> getAllLines() {
        return unmodifiableList(allLines);
    }

    /**
     * Returns the captured regular and error output, with the lines separated by "\n".
     * 
     * @return the captured regular and error output as one string.
     */
    public String getAll() {
        return on('\n').join(allLines);
    }

    /**
     * Creates a {@link CapturingOverthereProcessOutputHandler}.
     * 
     * @return the created {@link CapturingOverthereProcessOutputHandler}.
     */
    public static CapturingOverthereProcessOutputHandler capturingHandler() {
        return new CapturingOverthereProcessOutputHandler();
    }

}
