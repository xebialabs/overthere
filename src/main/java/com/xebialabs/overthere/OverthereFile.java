/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.xebialabs.overthere.util.OverthereFileCopier;

/**
 * A file system object (file, directory, etc.) on a remote system that is accessible through an {@link OverthereConnection}.
 */
public abstract class OverthereFile {

	protected OverthereConnection connection;

	protected OverthereFile(OverthereConnection connection) {
		this.connection = connection;
	}

	/**
	 * The connection through which this file is accessible. If the connection is closed, this file may no longer be accessible.
	 */
	public final OverthereConnection getConnection() {
		return connection;
	}

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

	public OverthereFile getFile(String child) {
		return getConnection().getFile(this, child);
	}

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

	public void deleteRecursively() throws RuntimeIOException {
		if (isDirectory()) {
			for (OverthereFile each : listFiles()) {
				each.deleteRecursively();
			}
		}

		delete();
	}

	public abstract List<OverthereFile> listFiles();

	public abstract void mkdir();

	public abstract void mkdirs();

	public abstract void renameTo(OverthereFile dest);

	public final void copyTo(final OverthereFile dest) {
		dest.copyFrom(this);
	}

	protected void copyFrom(OverthereFile source) {
		OverthereFileCopier.copy(source, this, null);
	}

	/**
	 * Subclasses MUST implement equals properly.
	 */
	@Override
	public abstract boolean equals(Object obj);

	/**
	 * Subclasses MUST implement hashCode properly.
	 */
	@Override
	public abstract int hashCode();

	/**
	 * Subclasses MUST implement toString properly.
	 */
	@Override
	public abstract String toString();

}
