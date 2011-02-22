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
package com.xebialabs.overthere;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * An implementation of the {@link CommandExecutionCallbackHandler} that captures the output in variables.
 */
public final class CapturingCommandExecutionCallbackHandler implements CommandExecutionCallbackHandler {

	private List<String> outputLines = new ArrayList<String>();

	private List<String> errorLines = new ArrayList<String>();

	private List<String> allLines = new ArrayList<String>();

	private boolean debug;

	/**
	 * Creates a <tt>CommandExecutionCallbackHandler</tt>.
	 */
	public CapturingCommandExecutionCallbackHandler() {
		this(false);
	}

	/**
	 * Creates a <tt>CommandExecutionCallbackHandler</tt> that can be configured to debug the output.
	 * 
	 * @param debug
	 *            if <tt>true</tt>, output is printed to <tt>System.out</tt> and <tt>System.err</tt> in addition to
	 *            being captured.
	 */
	public CapturingCommandExecutionCallbackHandler(boolean debug) {
		this.debug = debug;
	}

	public void handleOutputLine(String line) {
		if (debug) {
			System.out.println(line);
		}
		outputLines.add(line);
		allLines.add(line);
	}

	public void handleErrorLine(String line) {
		if (debug) {
			System.err.println(line);
		}
		errorLines.add(line);
		allLines.add(line);
	}

	public void handleOutput(char c) {
		// no-op
	}

	/**
	 * Returns the captured regular output lines.
	 * 
	 * @return a list of captured regular output lines.
	 */
	public List<String> getOutputLines() {
		return Collections.unmodifiableList(outputLines);
	}

	/**
	 * Returns the captured regular output, with the lines separated by "\n".
	 * 
	 * @return the captured regular output as one string.
	 */
	public String getOutput() {
		return StringUtils.join(outputLines, "\n");
	}

	/**
	 * Returns the captured error output lines.
	 * 
	 * @return a list of captured error output lines.
	 */
	public List<String> getErrorLines() {
		return Collections.unmodifiableList(errorLines);
	}

	/**
	 * Returns the captured error output, with the lines separated by "\n".
	 * 
	 * @return the captured error output as one string.
	 */
	public String getError() {
		return StringUtils.join(errorLines, "\n");
	}

	/**
	 * Returns the captured regular and error output lines.
	 * 
	 * @return a list of captured error regular and output lines.
	 */
	public List<String> getAllLines() {
		return Collections.unmodifiableList(allLines);
	}

	/**
	 * Returns the captured regular and error output, with the lines separated by "\n".
	 * 
	 * @return the captured regular and error output as one string.
	 */
	public String getAll() {
		return StringUtils.join(allLines, "\n");
	}

	public String toString() {
		return this.getClass().getName() + "[output=\"" + getOutput() + "\", error=\"" + getError() + "\"]";
	}

}

