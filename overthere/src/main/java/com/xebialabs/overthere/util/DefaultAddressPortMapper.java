package com.xebialabs.overthere.util;

import com.xebialabs.overthere.spi.AddressPortMapper;

import java.net.InetSocketAddress;

public class DefaultAddressPortMapper implements AddressPortMapper {
	@Override
	public InetSocketAddress map(InetSocketAddress address) {
		return address;
	}

	@Override
	public void close() {
		// Do nothing :-)
	}
}
