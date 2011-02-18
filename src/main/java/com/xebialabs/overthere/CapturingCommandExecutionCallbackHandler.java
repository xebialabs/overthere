/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
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
