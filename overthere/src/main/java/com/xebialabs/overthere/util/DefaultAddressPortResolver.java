package com.xebialabs.overthere.util;

import com.xebialabs.overthere.spi.AddressPortResolver;

import java.net.InetSocketAddress;

public class DefaultAddressPortResolver implements AddressPortResolver {
	@Override
	public InetSocketAddress resolve(InetSocketAddress address) {
		return address;
	}

	@Override
	public void close() {
		// Do nothing :-)
	}
}
