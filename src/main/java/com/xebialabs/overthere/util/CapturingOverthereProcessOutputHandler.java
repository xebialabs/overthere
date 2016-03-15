/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereProcessOutputHandler;

import java.util.ArrayList;
import java.util.List;

import static com.xebialabs.overthere.util.OverthereUtils.mkString;
import static java.util.Collections.unmodifiableList;

/**
 * An implementation of the {@link OverthereProcessOutputHandler} that captures the output in variables.
 *
 * @deprecated See {@link CapturingOverthereExecutionOutputHandler}
 */
@Deprecated
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
        return mkString(outputLines, '\n');
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
        return mkString(errorLines, '\n');
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
        return mkString(allLines, '\n');
    }

    /**
     * Creates a {@link CapturingOverthereProcessOutputHandler}.
     *
     * @return the created {@link CapturingOverthereProcessOutputHandler}.
     * @deprecated Use {@link com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler#capturingHandler()}
     */
    @Deprecated
    public static CapturingOverthereProcessOutputHandler capturingHandler() {
        return new CapturingOverthereProcessOutputHandler();
    }



}
