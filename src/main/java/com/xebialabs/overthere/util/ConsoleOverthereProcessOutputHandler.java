/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
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

/**
 * Implementation of the {@link OverthereProcessOutputHandler} interface that prints the output to the console ({@link System#out} and {@link System#err}).
 */
public class ConsoleOverthereProcessOutputHandler implements OverthereProcessOutputHandler {

	private ConsoleOverthereProcessOutputHandler() {
	}

	@Override
	public void handleOutputLine(final String line) {
		System.out.println(line);
	}

	@Override
	public void handleErrorLine(final String line) {
		System.err.println(line);
	}

	@Override
	public void handleOutput(final char c) {
		// no-op
	}

	/**
	 * Creates a {@link ConsoleOverthereProcessOutputHandler}.
	 * 
	 * @return the created {@link ConsoleOverthereProcessOutputHandler}.
	 */
	public static ConsoleOverthereProcessOutputHandler consoleHandler() {
		return new ConsoleOverthereProcessOutputHandler();
	}

}

