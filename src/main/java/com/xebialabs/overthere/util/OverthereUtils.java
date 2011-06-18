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
			final OutputStream out = to.getOutputStream();
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
					return to.getOutputStream();
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

	public static String getName(String path) {
		int lastSlash = path.lastIndexOf('/');
		if(lastSlash >= 0) {
			return path.substring(lastSlash + 1);
		}

		int lastBackslash = path.lastIndexOf('\\');
		if(lastBackslash >= 0) {
			return path.substring(lastBackslash + 1);
		}
		
		return path;
	}
	
	public static String getBaseName(String name) {
		int dot = name.lastIndexOf('.');
		if(dot >= 0) {
			return name.substring(0, dot);
		}
		return name;
	}
	
	public static String getExtension(String name) {
		int dot = name.lastIndexOf('.');
		if(dot >= 0) {
			return name.substring(dot + 1);
		}
		return name;
	}

}
