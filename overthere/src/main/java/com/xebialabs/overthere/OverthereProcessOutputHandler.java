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
 * Handler that gets sent the output (stdout and stderr) of an {@link OverthereProcess}.
 */
public interface OverthereProcessOutputHandler {

	/**
	 * Invoked when an executed command generates a single character of output (stdout).
	 * 
	 * @param c
	 *            the character of output generated.
	 */
	void handleOutput(char c);

	/**
	 * Invoked when an executed command generated a line of output (stdout).
	 * 
	 * @param line
	 *            the line of output generated.
	 */
	void handleOutputLine(String line);

	/**
	 * Invoked when an executed command generated a line of error (stderr).
	 * 
	 * @param line
	 *            the line of output generated.
	 */
	void handleErrorLine(String line);

}
