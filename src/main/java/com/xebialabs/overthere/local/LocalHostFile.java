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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.HostConnection;
import com.xebialabs.overthere.common.AbstractHostFile;


/**
 * A local host file
 */
class LocalHostFile extends AbstractHostFile implements HostFile {

	private File file;

	public LocalHostFile(HostConnection connection, File file) {
		super(connection);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public String getPath() {
		return file.getPath();
	}

	public String getName() {
		return file.getName();
	}

	public String getParent() {
		return file.getParent();
	}

	public boolean exists() {
		return file.exists();
	}

	public boolean isDirectory() {
		return file.isDirectory();
	}

	public long length() throws RuntimeIOException {
		return file.length();
	}

	public boolean canRead() {
		return file.canRead();
	}

	public boolean canWrite() {
		return file.canWrite();
	}

	public boolean canExecute() {
		if (connection.getHostOperatingSystem() == OperatingSystemFamily.UNIX) {
			return executeStat().canExecute;
		} else {
			return canRead();
		}
	}

	public List<String> list() throws RuntimeIOException {
		String[] listArray = file.list();
		if (listArray == null) {
			return null;
		} else {
			return Arrays.asList(listArray);
		}
	}

	public void mkdir() throws RuntimeIOException {
		if (!file.mkdir()) {
			throw new RuntimeIOException("Cannot create directory " + file.getPath());
		}
	}

	public void mkdirs() throws RuntimeIOException {
		if (file.exists()) {
			return;
		}
		if (!file.mkdirs()) {
			throw new RuntimeIOException("Cannot create directory " + file.getPath() + " or one of its parent directories");
		}
	}

	public void moveTo(HostFile destFile) throws RuntimeIOException {
		if (destFile instanceof LocalHostFile) {
			File destLocalFile = ((LocalHostFile) destFile).getFile();
			if (!file.renameTo(destLocalFile)) {
				throw new RuntimeIOException("Cannot move/rename " + this + " to " + destFile);
			}
		} else {
			throw new RuntimeIOException("Cannot move/rename local file/directory " + this + " to non-local file/directory " + destFile);
		}
	}

	public boolean delete() {
		if (file.exists()) {
			if (!file.delete()) {
				throw new RuntimeIOException("Cannot delete " + file.getPath());
			}
			return true;
		} else {
			return false;
		}
	}

	public InputStream get() throws RuntimeIOException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public OutputStream put(long length) throws RuntimeIOException {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public String toString() {
		return file.toString();
	}

	@Override
	protected int executeCommand(CommandExecutionCallbackHandler handler, String... command) {
		return connection.execute(handler, command);
	}
	
}

