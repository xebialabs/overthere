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

import static com.xebialabs.itest.ItestHostFactory.getItestProperty;

class ExistingItestHost implements ItestHost {

	private final String hostname;

	public ExistingItestHost(String hostLabel) {
		this.hostname = getItestProperty(hostLabel + ".hostname", hostLabel);
	}

	@Override
	public void setup() {
		// no-op
	}

	@Override
	public void teardown() {
		// no-op
	}

	@Override
	public String getHostName() {
		return hostname;
	}

	@Override
    public int getPort(int port) {
	    return port;
    }

}

