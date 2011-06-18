package com.xebialabs.overthere;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface OverthereFile {

	/**
	 * The connection through which this file is accessible. If the connection is closed, this file may no longer be accessible.
	 */
	OverthereConnection getConnection();

	/**
	 * The full path of the file on the remote system.
	 * 
	 * @return the path of the file.
	 */
	String getPath();

	/**
	 * The name of the file on the remote system.
	 * 
	 * @return the name of the file.
	 */
	String getName();

	/**
	 * The parent file of 
	 * @return
	 */
	OverthereFile getParentFile();

	OverthereFile getFile(String child);

	long length();

	long lastModified();

	boolean exists();

	boolean isDirectory();

	boolean isHidden();

	boolean canRead();

	boolean canWrite();

	boolean canExecute();

	InputStream getInputStream();

	OutputStream getOutputStream(long length);

	void delete();

	void deleteRecursively() throws RuntimeIOException;

	List<OverthereFile> listFiles();

	void mkdir();

	void mkdirs();

	void renameTo(OverthereFile dest);

	void copyTo(final OverthereFile dest);

}