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

package com.xebialabs.overthere;

import static com.xebialabs.itest.ItestHostFactory.getItestHost;

import com.xebialabs.itest.ItestHost;

/**
 * Base class for all Overthere connection itests that use an {@link ItestHost}.
 */
public abstract class ItestHostOverthereConnectionItestBase extends OverthereConnectionItestBase {

	protected static ItestHost host;

	public static void doSetupItestHost(String itestHostLabel) {
		host = getItestHost(itestHostLabel);
		host.setup();
	}
	
	public static void doTeardownItestHost() {
		if(host != null) {
			host.teardown();
		}
	}

}

