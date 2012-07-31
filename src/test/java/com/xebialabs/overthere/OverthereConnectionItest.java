package com.xebialabs.overthere;

import static com.google.common.collect.Maps.newHashMap;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.JUMPSTATION;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.ITest;

/**
 * Base class for all parametrized Overthere connection itests that use a
 * {@link com.xebialabs.overcast.CloudHost}.
 */
public class OverthereConnectionItest extends OverthereConnectionItestBase implements ITest {

	protected final ConnectionOptions partialOptions;
	private static final Map<String, AtomicInteger> timesHostNeeded = newHashMap();
    private final String testName;

	public OverthereConnectionItest(String testName, String protocol, ConnectionOptions partialOptions, String expectedConnectionClassName, String host) throws Exception {
		this.testName = testName;
		this.protocol = protocol;
		this.partialOptions = partialOptions;
		this.expectedConnectionClassName = expectedConnectionClassName;
        this.hostname = host;
        registerHostNeeded(host);
	}

	private static void registerHostNeeded(String host) {
		if (!timesHostNeeded.containsKey(host)) {
			timesHostNeeded.put(host, new AtomicInteger(0));
		}
		timesHostNeeded.get(host).incrementAndGet();
	}

    @Override
    public String getTestName() {
        return testName;
    }

    @Override
	protected void doInitHost() {
		CloudHostHolder.setupHost(hostname);
	}

	@Override
	protected void doTeardownHost() {
		if (timesHostNeeded.get(hostname).decrementAndGet() == 0) {
			CloudHostHolder.teardownHost(hostname);
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
