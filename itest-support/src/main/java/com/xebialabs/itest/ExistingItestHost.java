package com.xebialabs.itest;

import static com.xebialabs.itest.ItestHostFactory.getItestProperty;

class ExistingItestHost implements ItestHost {

	private final String hostname;

	public ExistingItestHost(String hostLabel) {
		this.hostname = getItestProperty(hostLabel + ".hostname", hostLabel);
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
