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
