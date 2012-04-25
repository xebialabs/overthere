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
