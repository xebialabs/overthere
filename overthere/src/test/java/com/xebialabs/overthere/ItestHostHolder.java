package com.xebialabs.overthere;

import static com.google.common.collect.Maps.newHashMap;
import static com.xebialabs.itest.ItestHostFactory.getItestHost;

import com.xebialabs.itest.ItestHost;

import java.util.Map;

/**
 * Base class for all Overthere connection itests that use an {@link ItestHost}.
 */
public final class ItestHostHolder {

	protected static Map<String, ItestHost> hosts = newHashMap();

	public static void doSetupItestHost(String itestHostLabel) {
		if (hosts.get(itestHostLabel) == null) {
			ItestHost host = getItestHost(itestHostLabel);
			host.setup();
			hosts.put(itestHostLabel, host);
		}
	}
	
	public static void doTeardownItestHost(String hostname) {
		if(hosts.get(hostname) != null) {
			hosts.get(hostname).teardown();
			hosts.remove(hostname);
		}
	}

	public static ItestHost getHost(String hostname) {
		return hostname != null ? hosts.get(hostname) : null;
	}
}
