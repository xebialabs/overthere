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
package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.BaseOverthereFile;

/**
 * A file on a host connected through SSH.
 */
abstract class SshFile<C extends SshConnection> extends BaseOverthereFile<C> {

	protected String path;

	/**
	 * Constructs an SshOverthereFile
	 * 
	 * @param connection
	 *            the connection to the host
	 * @param path
	 *            the path of the file on the host
	 */
	SshFile(C connection, String path) {
		super(connection);
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getName() {
		String fileSep = connection.getHostOperatingSystem().getFileSeparator();
		int lastFileSepPos = path.lastIndexOf(fileSep);
		if (lastFileSepPos < 0) {
			return path;
		} else {
			return path.substring(lastFileSepPos + 1);
		}
	}

	@Override
	public OverthereFile getParentFile() {
		String fileSep = connection.getHostOperatingSystem().getFileSeparator();
		int lastFileSepPos = path.lastIndexOf(fileSep);
		if (lastFileSepPos < 0 || path.equals(fileSep)) {
			return null;
		} else if (lastFileSepPos == 0) {
			// the parent of something in the root directory is the root
			// directory itself.
			return connection.getFile(fileSep);
		} else {
			return connection.getFile(path.substring(0, lastFileSepPos));
		}

	}

	@Override
	public void delete() throws RuntimeIOException {
		if (exists()) {
			if (isDirectory()) {
				deleteDirectory();
			} else {
				deleteFile();
			}
		}
	}

	@Override
	public boolean isHidden() {
		return getName().startsWith(".");
	}

	protected abstract void deleteFile();

	protected abstract void deleteDirectory();

	protected int executeCommand(OverthereProcessOutputHandler handler, CmdLine commandLine) {
		return connection.execute(handler, commandLine);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SshFile)) {
			return false;
		}

		return path.equals(((SshFile<?>) obj).getPath());
	}

	@Override
	public int hashCode() {
		return path.hashCode();
	}

	@Override
	public String toString() {
		return connection + path;
	}

}
