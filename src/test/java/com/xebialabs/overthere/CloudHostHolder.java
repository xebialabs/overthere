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
        if (hosts.get(hostname) != null) {
            hosts.get(hostname).teardown();
            hosts.remove(hostname);
        }
    }

    public static CloudHost getHost(String hostname) {
        return hostname != null ? hosts.get(hostname) : null;
    }
}
