package com.xebialabs.overthere.spi;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.xebialabs.overthere.HostConnection;
import com.xebialabs.overthere.OverthereFile;

@SuppressWarnings("serial")
public abstract class RemoteOverthereFile extends OverthereFile {

	private String path;

	protected RemoteOverthereFile(HostConnection connection, String path) {
		super(connection, path);
		this.path = path;
	}

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public boolean isAbsolute() {
		return false;
	}

	@Override
	public String getAbsolutePath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public File getAbsoluteFile() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCanonicalPath() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public File getCanonicalFile() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public abstract boolean exists();

	@Override
	public abstract long length();

	@Override
	public abstract long lastModified();

	@Override
	public abstract boolean isDirectory();

	@Override
	public abstract boolean isHidden();

	@Override
	public abstract boolean canRead();

	@Override
	public abstract boolean canWrite();

	@Override
	public abstract boolean canExecute();

	@Override
	public boolean setReadOnly() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setLastModified(long time) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setReadable(boolean readable, boolean ownerOnly) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setReadable(boolean readable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setWritable(boolean writable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setExecutable(boolean executable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public abstract String[] list();

	@Override
	public abstract boolean mkdir();

	@Override
	public abstract boolean mkdirs();

	@Override
	public abstract boolean renameTo(File dest);

	@Override
	public abstract boolean delete();

	@Override
	public long getTotalSpace() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getFreeSpace() {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getUsableSpace() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(File pathname) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void deleteOnExit() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean createNewFile() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public URL toURL() throws MalformedURLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public URI toURI() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return getPath() + " on " + connection;
	}

}
