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

import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.RemoteOverthereFile;

/**
 * A file on a host connected through SSH.
 */
@SuppressWarnings("serial")
abstract class SshOverthereFile extends RemoteOverthereFile {

	protected SshHostConnection sshHostConnection;

	/**
	 * Constructs an SshOverthereFile
	 * 
	 * @param connection
	 *            the connection to the host
	 * @param path
	 *            the path of the file on the host
	 */
	SshOverthereFile(SshHostConnection connection, String path) {
		super(connection, path);
		sshHostConnection = connection;
	}

	public boolean delete() throws RuntimeIOException {
		if (!exists()) {
			return false;
		} else if (isDirectory()) {
			deleteDirectory();
			return true;
		} else {
			deleteFile();
			return true;
		}
	}

	protected abstract void deleteFile();

	protected abstract void deleteDirectory();

	protected int executeCommand(CommandExecutionCallbackHandler handler, String... command) {
		return sshHostConnection.execute(handler, command);
	}

}
