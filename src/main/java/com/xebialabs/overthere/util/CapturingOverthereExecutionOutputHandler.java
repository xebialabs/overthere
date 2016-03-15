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

import com.xebialabs.overthere.OverthereExecutionOutputHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.xebialabs.overthere.util.OverthereUtils.mkString;
import static java.util.Collections.unmodifiableList;

/**
 * An implementation of the {@link com.xebialabs.overthere.OverthereExecutionOutputHandler} that captures the output in variables.
 */
public class CapturingOverthereExecutionOutputHandler implements OverthereExecutionOutputHandler {

    private List<String> outputLines = Collections.synchronizedList(new ArrayList<String>());

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
        return mkString(outputLines, '\n');
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
