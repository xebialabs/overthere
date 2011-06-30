package com.xebialabs.itest;

/**
 * Represents a host that is used for an integration test.
 */
public interface ItestHost {

	/**
	 * Ensures the host is available for the integration test. To be called before the integration is started.
	 */
	void setup();

	/**
	 * Releases the host resources. To be called after the integration test has finished.
	 */
	void teardown();

	/**
	 * Returns the host name of the host. Can only be called after {@link #setup()} has been invoked.
	 * 
	 * @return the host name.
	 */
	String getHostName();

}
