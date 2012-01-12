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

package com.xebialabs.itest;

/**
 * Represents a host that is used for an integration test.
 */
public interface ItestHost {

	/**
	 * Ensures the host is available for the integration test. To be called before the integration is started.
	 */
	void setup();

	/**
	 * Releases the host resources. To be called after the integration test has finished.
	 */
	void teardown();

	/**
	 * Returns the name of the host to connect to. Can only be called after {@link #setup()} has been invoked.
	 * 
	 * @return the host name.
	 */
	String getHostName();

	/**
	 * Translates a target port number to the port number to connect to. Can only be called after {@link #setup()} has been invoked.
	 * 
	 * @param port
	 *            the target port number
	 * 
	 * @return the translated port number.
	 */
	int getPort(int port);

}

