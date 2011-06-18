package com.xebialabs.overthere.util;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.local.LocalConnection.getLocalConnection;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.spi.BaseOverthereFile;

class ByteArrayFile extends BaseOverthereFile<LocalConnection> {

	private String path;

	private byte[] contents;

	ByteArrayFile(String path, byte[] contents) {
		super((LocalConnection) getLocalConnection());
		this.path = path;
		this.contents = contents;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getName() {
		return OverthereUtils.getName(path);
	}

	@Override
	public OverthereFile getParentFile() {
		return null;
	}

	@Override
	public OverthereFile getFile(String child) {
		return null;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return false;
	}

	@Override
	public boolean canExecute() {
		return false;
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public long lastModified() {
		return 0;
	}

	@Override
	public long length() {
		return contents.length;
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(contents);
	}

	@Override
	public OutputStream getOutputStream() {
		throw new UnsupportedOperationException("Cannot write to a byte array file");
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("Cannot delete a byte array file");
	}

	@Override
	public void deleteRecursively() {
		throw new UnsupportedOperationException("Cannot delete a byte array file");
	}

	@Override
	public List<OverthereFile> listFiles() {
		return newArrayList();
	}

	@Override
	public void mkdir() {
		throw new UnsupportedOperationException("Cannot mkdir a byte array file");
	}

	@Override
	public void mkdirs() {
		throw new UnsupportedOperationException("Cannot mkdirs a byte array file");
	}

	@Override
	public void renameTo(OverthereFile dest) {
		throw new UnsupportedOperationException("Cannot rename a byte array file");
	}

	@Override
	public String toString() {
		return "byte-array file for path " + path;
	}

}
