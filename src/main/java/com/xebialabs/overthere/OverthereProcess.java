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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents an executing process.
 */
public interface OverthereProcess {

	/**
	 * Returns an output stream that is connected to the standard input stream (stdin) of the running command.
	 * 
	 * @return the input stream
	 */
	OutputStream getStdin();

	/**
	 * Returns an input stream that is connected to the standard output stream (stdout) of the running command.
	 * 
	 * @return the output stream
	 */
	InputStream getStdout();

	/**
	 * Returns an input stream that is connected to the standard error stream (stderr) of the running command.
	 * 
	 * @return the output stream
	 */
	InputStream getStderr();

	/**
	 * Waits for the command to complete its execution. Note that {@link #destroy()} still needs to be invoked to clean up any resources that may be left!
	 * 
	 * @return the exit value of the executed command
	 * 
	 * @throws InterruptedException
	 *             if this method was interrupted
	 */
	int waitFor() throws InterruptedException;

	/**
	 * Destroys the executing process.
	 */
	void destroy();

}
