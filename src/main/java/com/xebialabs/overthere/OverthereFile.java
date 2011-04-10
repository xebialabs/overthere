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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;

/**
 * A reference to a file on a host. This object is always associated with a {@link HostConnection}.
 * 
 * <b>N.B.:</b> Even though {@link File} is {@link Serializable}, instances of this class may not, depending upon whether or not their implementations of the
 * {@link HostConnection} interface are serializable.
 */
@SuppressWarnings("serial")
public abstract class OverthereFile extends File {

	protected HostConnection connection;

	protected OverthereFile(HostConnection connection, String path) {
		super(path);
		this.connection = connection;
	}

	/**
	 * Returns the connection this file is associated with.
	 * 
	 * @return the connection
	 */
	public HostConnection getConnection() {
		return connection;
	}

	/**
	 * Returns a new HostFile with this file as its parent. Identical to invoking <code>this.getConnection().getFile(this, name)</code>
	 * 
	 * @param name
	 *            the name of the file in this directory
	 * @return a reference to the file in this directory
	 */
	public OverthereFile getFile(String name) {
		return connection.getFile(this, name);
	}

	@Override
	public String getName() {
		String fileSep = connection.getHostOperatingSystem().getFileSeparator();
		int lastFileSepPos = getPath().lastIndexOf(fileSep);
		if (lastFileSepPos < 0) {
			return getPath();
		} else {
			return getPath().substring(lastFileSepPos + 1);
		}
	}

	@Override
	public String getParent() {
		String fileSep = connection.getHostOperatingSystem().getFileSeparator();
		int lastFileSepPos = getPath().lastIndexOf(fileSep);
		if (lastFileSepPos < 0 || getPath().equals(fileSep)) {
			return null;
		} else if (lastFileSepPos == 0) {
			// the parent of something in the root directory is the root
			// directory itself.
			return fileSep;
		} else {
			return getPath().substring(0, lastFileSepPos);
		}

	}

	@Override
	public OverthereFile getParentFile() {
		String parent = getParent();
		if (parent == null || parent.length() == 0) {
			return null;
		} else {
			return getConnection().getFile(parent);
		}
	}

	@Override
	public OverthereFile[] listFiles() {
		String[] filenames = list();
		if (filenames == null) {
			return null;
		} else {
			OverthereFile[] files = new OverthereFile[filenames.length];
			for (int i = 0; i < filenames.length; i++) {
				files[i] = connection.getFile(this, filenames[i]);
			}
			return files;
		}
	}

	/**
	 * Deletes this file or directory recursively. If this is a directory, first its contents are deleted and then the directory itself. If this is a file, this
	 * method behaves identically to {@link #delete()}.
	 * 
	 * @return <code>true</code> if and only if the file or directory is successfully deleted; <code>false</code> otherwise
	 */
	public boolean deleteRecursively() {
		if (!exists()) {
			return false;
		}

		if (isDirectory()) {
			for (OverthereFile f : listFiles()) {
				if (!f.getPath().startsWith(getPath())) {
					continue;
				}

				if (!f.deleteRecursively()) {
					return false;
				}
			}
		}

		return delete();
	}

	/**
	 * Opens this file for reading.
	 * 
	 * @return the InputStream connected to the file.
	 */
	public abstract InputStream get();

	/**
	 * Copies the content of this file to a stream.
	 * 
	 * @param out
	 *            the stream to copy to
	 */
	public void get(OutputStream out) {
		try {
			InputStream in = get();
			try {
				IOUtils.copy(in, out);
			} finally {
				IOUtils.closeQuietly(in);
			}
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	/**
	 * Copies the content of this file to another file.
	 * 
	 * @param dest
	 *            the file to copy to
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public void get(File dest) {
		try {
			OutputStream out;
			if (dest instanceof OverthereFile) {
				out = ((OverthereFile) dest).put(length());
			} else {
				out = new FileOutputStream(dest);
			}

			try {
				get(out);
			} finally {
				out.close();
			}
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	/**
	 * Opens this file for writing.
	 * 
	 * @param length
	 *            the number of bytes that will be written to the stream
	 * @return the OutputStream connected to the file.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public abstract OutputStream put(long length);

	/**
	 * Copies the contents of a stream to this file.
	 * 
	 * @param in
	 *            the stream to copy from
	 * @param length
	 *            the number of bytes that will be written to the stream
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public void put(InputStream in, long length) {
		try {
			OutputStream out = put(length);
			try {
				IOUtils.copy(in, out);
			} finally {
				IOUtils.closeQuietly(out);
			}
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}

	}

	/**
	 * Copies the contact of another file to this file.
	 * 
	 * @param src
	 *            the file to copy from
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	public void put(File src) {
		try {
			InputStream in;
			if (src instanceof OverthereFile) {
				in = ((OverthereFile) src).get();
			} else {
				in = new FileInputStream(src);
			}
			try {
				put(in, src.length());
			} finally {
				in.close();
			}
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}

	}

}
