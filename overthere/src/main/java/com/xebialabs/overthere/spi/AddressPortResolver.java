package com.xebialabs.overthere.spi;

import java.io.Closeable;
import java.net.InetSocketAddress;

/**
 * Resolves an {@link java.net.InetSocketAddress} to another {@link java.net.InetSocketAddress}.
 */
public interface AddressPortResolver extends Closeable {

	InetSocketAddress resolve(InetSocketAddress address);

	/**
	 * Closes the resolver. Does not throw {@link java.io.IOException} but can throw {@link com.xebialabs.overthere.RuntimeIOException}
	 */
	void close();
}
