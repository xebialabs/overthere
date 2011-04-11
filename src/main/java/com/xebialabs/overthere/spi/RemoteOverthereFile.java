package com.xebialabs.overthere.spi;

import java.io.File;

import com.xebialabs.overthere.HostConnection;
import com.xebialabs.overthere.OverthereFile;

@SuppressWarnings("serial")
public abstract class RemoteOverthereFile extends OverthereFile {

	protected RemoteOverthereFile(HostConnection connection, String path) {
		super(connection, path);
	}

	@Override
	public abstract boolean exists();

	@Override
	public abstract boolean isDirectory();

	@Override
	public abstract long length();

	@Override
	public abstract boolean canRead();

	@Override
	public abstract boolean canWrite();

	@Override
	public abstract boolean canExecute();

	@Override
	public abstract String[] list();

	/**
	 * TODO: Decide whether we want to change the error semantics for OverthereFile.mkdir, mkdirs and renameTo? Then implement consistently
	 */
	@Override
	public abstract boolean mkdir();

	@Override
	public abstract boolean mkdirs();

	@Override
	public abstract boolean renameTo(File dest);

	@Override
	public abstract boolean delete();

	@Override
	public void deleteOnExit() {
		// FIXME
	}

	@Override
	public String toString() {
		return getPath() + " on " + connection;
	}

}
