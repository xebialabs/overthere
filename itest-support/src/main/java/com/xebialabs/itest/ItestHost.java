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
	 * Returns the name of the host to connect to. Can only be called after {@link #setup()} has been invoked.
	 * 
	 * @return the host name.
	 */
	String getHostName();

	/**
	 * Translates a target port number to the port number to connect to. Can only be called after {@link #setup()} has been invoked.
	 * 
	 * @param port
	 *            the target port number
	 * 
	 * @return the translated port number.
	 */
	int getPort(int port);

}
