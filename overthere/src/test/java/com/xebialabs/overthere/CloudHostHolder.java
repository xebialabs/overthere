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

import java.util.Map;

import com.xebialabs.overcast.CloudHost;
import com.xebialabs.overcast.CloudHostFactory;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Base class for all Overthere connection itests that use an {@link CloudHost}.
 */
public final class CloudHostHolder {

	protected static Map<String, CloudHost> hosts = newHashMap();

	public static void setupHost(String hostLabel) {
		if (hosts.get(hostLabel) == null) {
			CloudHost host = CloudHostFactory.getCloudHost(hostLabel);
			host.setup();
			hosts.put(hostLabel, host);
		}
	}
	
	public static void teardownHost(String hostname) {
		if(hosts.get(hostname) != null) {
			hosts.get(hostname).teardown();
			hosts.remove(hostname);
		}
	}

	public static CloudHost getHost(String hostname) {
		return hostname != null ? hosts.get(hostname) : null;
	}
}

