package com.xebialabs.overthere;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface OverthereFile {

	/**
	 * The connection through which this file is accessible. If the connection is closed, this file may no longer be accessible.
	 */
	public abstract OverthereConnection getConnection();

	/**
	 * The full path of the file on the remote system.
	 * 
	 * @return the path of the file.
	 */
	public abstract String getPath();

	/**
	 * The name of the file on the remote system.
	 * 
	 * @return the name of the file.
	 */
	public abstract String getName();

	/**
	 * The parent file of 
	 * @return
	 */
	public abstract OverthereFile getParentFile();

	public abstract OverthereFile getFile(String child);

	public abstract long length();

	public abstract long lastModified();

	public abstract boolean exists();

	public abstract boolean isDirectory();

	public abstract boolean isHidden();

	public abstract boolean canRead();

	public abstract boolean canWrite();

	public abstract boolean canExecute();

	public abstract InputStream getInputStream();

	public abstract OutputStream getOutputStream(long length);

	public abstract void delete();

	public abstract void deleteRecursively() throws RuntimeIOException;

	public abstract List<OverthereFile> listFiles();

	public abstract void mkdir();

	public abstract void mkdirs();

	public abstract void renameTo(OverthereFile dest);

	public abstract void copyTo(final OverthereFile dest);

}