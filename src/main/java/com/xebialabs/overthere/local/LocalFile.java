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
package com.xebialabs.overthere.local;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.BaseOverthereFile;

/**
 * A local file.
 */
public class LocalFile extends BaseOverthereFile<LocalConnection> {

	protected File file;

	public LocalFile(LocalConnection connection, File file) {
		super(connection);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	@Override
	public String getPath() {
		return file.getPath();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public OverthereFile getParentFile() {
		return getConnection().getFile(file.getParent());
	}

	@Override
    public long lastModified() {
	    return file.lastModified();
    }

	@Override
	public long length() {
		return file.length();
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public boolean isFile() {
		return file.isFile();
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}
	
	@Override
	public boolean isHidden() {
		return file.isHidden();
	}

	@Override
	public boolean canRead() {
		return file.canRead();
	}

	@Override
	public boolean canWrite() {
		return file.canWrite();
	}

	@Override
	public boolean canExecute() {
		return file.canExecute();
	}

	@Override
	public void delete() {
		if (!file.delete()) {
			throw new RuntimeIOException("Cannot delete " + this);
		}
	}

	@Override
	public void mkdir() {
		if (!file.mkdir()) {
			throw new RuntimeIOException("Cannot mkdir " + this);
		}
	}

	@Override
	public void mkdirs() {
		if (!file.mkdirs()) {
			throw new RuntimeIOException("Cannot mkdir " + this);
		}
	}

	@Override
	public List<OverthereFile> listFiles() {
		List<OverthereFile> list = newArrayList();
		for (File each : file.listFiles()) {
			list.add(new LocalFile(connection, each));
		}
		return list;
	}

	@Override
	public void renameTo(OverthereFile dest) {
		if (!(dest instanceof LocalFile)) {
			throw new RuntimeIOException("Destination is not a " + LocalFile.class.getName());
		}

		if (!file.renameTo(((LocalFile) dest).file)) {
			throw new RuntimeIOException("Cannot rename " + this + " to " + dest);
		}

	}

	@Override
	public InputStream getInputStream() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException exc) {
			throw new RuntimeIOException("Cannot open " + this + " for reading", exc);
		}
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException exc) {
			throw new RuntimeIOException("Cannot open " + this + " for writing", exc);
		}
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof LocalFile))
			return false;

		return this.file.equals(((LocalFile) that).file);
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public String toString() {
		return file.toString();
	}

	public static OverthereFile valueOf(File f) {
		// Creating LocalConnection directly instead of through Overthere.getConnection() to prevent log messages from appearing
		LocalConnection localConnectionThatWillNeverBeDisconnected = new LocalConnection("local", new ConnectionOptions());
		return new LocalFile(localConnectionThatWillNeverBeDisconnected, f);
	}

}
