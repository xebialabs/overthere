package com.xebialabs.overthere;

import static com.xebialabs.itest.ItestHostFactory.getItestHost;

import org.junit.AfterClass;

import com.xebialabs.itest.ItestHost;

/**
 * Base class for all Overthere connection itests that use an {@link ItestHost}.
 */
public abstract class ItestHostOverthereConnectionItestBase extends OverthereConnectionItestBase {

	protected static ItestHost host;

	public static void setupItestHost(String itestHostLabel) {
		host = getItestHost(itestHostLabel);
		host.setup();
	}
	
	@AfterClass
	public static void teardownHost() {
		if(host != null) {
			host.teardown();
		}
	}

}
