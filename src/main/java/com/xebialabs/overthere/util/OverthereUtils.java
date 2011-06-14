package com.xebialabs.overthere.util;

import static com.google.common.io.ByteStreams.copy;
import static com.google.common.io.Closeables.close;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import com.google.common.io.ByteStreams;
import com.google.common.io.OutputSupplier;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

public class OverthereUtils {

	public static void write(final InputStream from, final long length, final OverthereFile to) {
		try {
			final OutputStream out = to.getOutputStream(length);
			try {
				copy(from, out);
			} finally {
				close(out, false);
			}
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot copy inputstream to " + to, exc);
		}
	}

	public static void write(final byte[] from, final OverthereFile to) {
		try {
			ByteStreams.write(from, new OutputSupplier<OutputStream>() {
				@Override
				public OutputStream getOutput() throws IOException {
					return to.getOutputStream(from.length);
				}
			});
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot copy inputstream to " + to, exc);
		}

	}

	public static void write(final String from, final String encoding, final OverthereFile to) {
		try {
			write(from.getBytes(encoding), to);
		} catch (UnsupportedEncodingException exc) {
			throw new RuntimeIOException("Cannot write string to " + to, exc);
		}
	}

}
