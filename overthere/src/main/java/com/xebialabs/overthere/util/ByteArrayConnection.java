package com.xebialabs.overthere.util;

import com.xebialabs.overthere.BaseOverthereConnection;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;

class ByteArrayConnection extends BaseOverthereConnection {

	protected ByteArrayConnection(String protocol, ConnectionOptions options) {
	    super(protocol, options, false);
    }

	@Override
	protected void doClose() {
		// no-op
	}

	@Override
	public OverthereFile getFile(String hostPath) {
		throw new UnsupportedOperationException("ByteArrayConnection has no functionality. Use only the created ByteArrayFile.");
	}

	@Override
	public OverthereFile getFile(OverthereFile parent, String child) {
		throw new UnsupportedOperationException("ByteArrayConnection has no functionality. Use only the created ByteArrayFile.");
	}

	@Override
	protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
		throw new UnsupportedOperationException("ByteArrayConnection has no functionality. Use only the created ByteArrayFile.");
	}

	@Override
	public String toString() {
		return "byte_array://";
	}

}
