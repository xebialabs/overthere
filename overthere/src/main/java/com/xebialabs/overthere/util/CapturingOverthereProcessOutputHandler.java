/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.util;

import com.xebialabs.overthere.OverthereProcessOutputHandler;

import java.util.ArrayList;
import java.util.List;

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

	public void handleOutputLine(final String line) {
		outputLines.add(line);
		allLines.add(line);
	}

	public void handleErrorLine(final String line) {
		errorLines.add(line);
		allLines.add(line);
	}

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

