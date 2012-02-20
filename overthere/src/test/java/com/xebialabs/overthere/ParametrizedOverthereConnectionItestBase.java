package com.xebialabs.overthere;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;

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
	}

}
