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

