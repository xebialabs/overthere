package com.xebialabs.overthere;

import static com.xebialabs.itest.ItestHostFactory.getItestHost;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.xebialabs.itest.ItestHost;

/**
 * Base class for all Overthere connection itests that use an {@link ItestHost}.
 */
public abstract class ItestHostOverthereConnectionItestBase extends OverthereConnectionItestBase {

	/**
	 * The label of the itest host to use for these tests. Use a static initializer in the subclass to set this field.
	 */
	protected static String itestHostLabel;

	protected static ItestHost host;

	@BeforeClass
	public static void setupHost() {
		host = getItestHost(itestHostLabel);
		host.setup();
	}
	
	@AfterClass
	public static void teardownHost() {
		if(host != null) {
			host.teardown();
		}
	}

}
