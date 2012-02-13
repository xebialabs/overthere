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

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.TUNNEL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.LOCAL_PORT_FORWARDS;

import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.xebialabs.itest.ItestHost;

/**
 * Base class for all parametrized Overthere connection itests that use an {@link ItestHost}.
 */
@RunWith(Parameterized.class)
public abstract class ParametrizedOverthereConnectionItestBase extends ItestHostOverthereConnectionItestBase {

	protected final ConnectionOptions partialOptions;
	
	public ParametrizedOverthereConnectionItestBase(String type, ConnectionOptions partialOptions, String expectedConnectionClassName) {
		this.type = type;
		this.partialOptions = partialOptions;
		this.expectedConnectionClassName = expectedConnectionClassName;
	}

	@Override
	protected void setTypeAndOptions() throws Exception {
		options = new ConnectionOptions(partialOptions);
		options.set(ADDRESS, host.getHostName());

		ConnectionOptions tunnelOptions = options.getOptional(TUNNEL);
		if (tunnelOptions != null) {
			tunnelOptions.set(ADDRESS, host.getHostName());
			tunnelOptions.set(LOCAL_PORT_FORWARDS, ((String) tunnelOptions.get(LOCAL_PORT_FORWARDS)).replace("TARGET", host.getHostName()));
		}
	}

}

