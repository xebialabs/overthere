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

import static com.google.common.collect.Maps.newHashMap;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.JUMPSTATION;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import nl.javadude.assumeng.AssumptionListener;

import org.testng.annotations.Listeners;

import com.xebialabs.itest.ItestHost;

/**
 * Base class for all parametrized Overthere connection itests that use an {@link ItestHost}.
 */
@Listeners({AssumptionListener.class})
public class OverthereConnectionItest extends OverthereConnectionItestBase {

	protected final ConnectionOptions partialOptions;
	private static final Map<String, AtomicInteger> timesHostNeeded = newHashMap();

	public OverthereConnectionItest(String type, ConnectionOptions partialOptions, String expectedConnectionClassName, String host) throws Exception {
		hostname = host;
		registerHostNeeded(host);
		this.type = type;
		this.partialOptions = partialOptions;
		this.expectedConnectionClassName = expectedConnectionClassName;
	}

	private void registerHostNeeded(String host) {
		if (!timesHostNeeded.containsKey(host)) {
			timesHostNeeded.put(host, new AtomicInteger(0));
		}
		timesHostNeeded.get(host).incrementAndGet();
	}

	@Override
	protected void doInitHost() {
		ItestHostHolder.doSetupItestHost(hostname);
	}

	@Override
	protected void doTeardownHost() {
		if (timesHostNeeded.get(hostname).decrementAndGet() == 0) {
			ItestHostHolder.doTeardownItestHost(hostname);
		}
	}

	@Override
	protected void setTypeAndOptions() throws Exception {
		options = new ConnectionOptions(partialOptions);
		options.set(ADDRESS, host.getHostName());

		ConnectionOptions tunnelOptions = options.getOptional(JUMPSTATION);
		if (tunnelOptions != null) {
			tunnelOptions.set(ADDRESS, host.getHostName());
		}

	}

}

