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
