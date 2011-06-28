package com.xebialabs.itest;

import java.util.Properties;

class ExistingItestHost implements ItestHost {

	private final String hostname;

	public ExistingItestHost(String hostId, Properties itestProperties) {
		this.hostname = itestProperties.getProperty(hostId + ".hostname", hostId);
	}

	@Override
	public void setup() {
		// no-op
	}

	@Override
	public void teardown() {
		// no-op
	}

	@Override
	public String getHostName() {
		return hostname;
	}

}
