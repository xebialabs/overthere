package com.xebialabs.itest;

class ExistingItestHost implements ItestHost {

	private final String hostId;

	public ExistingItestHost(String hostId) {
		this.hostId = hostId;
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
		return hostId;
	}

}
