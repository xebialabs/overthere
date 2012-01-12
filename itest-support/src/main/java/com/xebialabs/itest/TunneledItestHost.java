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

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;

class TunneledItestHost implements ItestHost {

	private final ItestHost actualItestHost;

	private final String tunnelUsername;
	
	private final String tunnelPassword;

	private final Map<Integer, Integer> portForwardMap;

	private SSHClient client;

	TunneledItestHost(ItestHost actualItestHost, String tunnelUsername, String tunnelPassword, Map<Integer, Integer> portForwardMap) {
	    this.actualItestHost = actualItestHost;
	    this.tunnelUsername = tunnelUsername;
	    this.tunnelPassword = tunnelPassword;
	    this.portForwardMap = portForwardMap;
    }

	@Override
    public void setup() {
		actualItestHost.setup();

        client = new SSHClient();
        client.addHostKeyVerifier(new LaxKeyVerifier());

        try {
    		client.connect(actualItestHost.getHostName(), 22);
	        client.authPassword(tunnelUsername, tunnelPassword);
	        for(Map.Entry<Integer, Integer> forwardedPort : portForwardMap.entrySet()) {
	        	int remotePort = forwardedPort.getKey();
	        	int localPort = forwardedPort.getValue();
	        	final LocalPortForwarder forwarder = client.newLocalPortForwarder(new InetSocketAddress("localhost", localPort), "localhost", remotePort);
	        	Thread forwarderThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
	                        forwarder.listen();
                        } catch (IOException ignore) {
                        }
					}
				}, "SSH port forwarder thread from local port " + localPort + " to " + actualItestHost.getHostName() + ":" + remotePort);
	        	forwarderThread.setDaemon(true);
	        	ItestHostFactory.logger.info("Starting {}", forwarderThread.getName());
	        	forwarderThread.start();
	        }
        } catch (IOException exc) {
        	throw new RuntimeException("Cannot set up tunnels to " + actualItestHost.getHostName(), exc);
        }
	}

	@Override
    public void teardown() {
		try {
	        client.disconnect();
        } catch (IOException ignored) {
        	//
        }

		actualItestHost.teardown();
    }

	@Override
    public String getHostName() {
	    return "localhost";
    }

	@Override
    public int getPort(int port) {
		checkArgument(portForwardMap.containsKey(port), "Port " + port + " is not tunneled");
	   	return portForwardMap.get(port);
	}
	
}

