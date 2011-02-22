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

/**
 * Implementation of the {@link CommandExecutionCallbackHandler} interface that sends its output to {@link System#out}
 * and {@link System#err}.
 */
	public class DebugCommandExecutionCallbackHandler implements CommandExecutionCallbackHandler {

	public void handleOutputLine(String line) {
		System.out.println(line);
	}

	public void handleErrorLine(String line) {
		System.err.println(line);
	}

	public void handleOutput(char c) {
		// no-op
	}
}

