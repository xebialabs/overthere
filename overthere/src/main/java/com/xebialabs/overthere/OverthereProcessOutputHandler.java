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

