package com.xebialabs.overthere.util;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;

class ByteArrayConnection extends OverthereConnection {

	protected ByteArrayConnection(String type, ConnectionOptions options) {
	    super(type, options);
    }

	@Override
	protected void doDisconnect() {
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
	public OverthereProcess startProcess(CmdLine commandLine) {
		throw new UnsupportedOperationException("ByteArrayConnection has no functionality. Use only the created ByteArrayFile.");
	}

	@Override
	public String toString() {
		return "byte_array://";
	}

}
