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
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.common.AbstractHostFile;

/**
 * A file on a host connected through SSH.
 */
abstract class SshHostFile extends AbstractHostFile implements HostFile {

	protected SshHostConnection sshHostSession;

	protected String remotePath;

	/**
	 * Constructs a SshHostFile
	 * 
	 * @param session
	 *            the connection connected to the host
	 * @param remotePath
	 *            the path of the file on the host
	 */
	SshHostFile(SshHostConnection session, String remotePath) {
		super(session);
		sshHostSession = session;
		this.remotePath = remotePath;
	}

	public String getPath() {
		return remotePath;
	}

	public String getName() {
		String fileSep = sshHostSession.getHostOperatingSystem().getFileSeparator();
		int lastFileSepPos = remotePath.lastIndexOf(fileSep);
		if (lastFileSepPos < 0) {
			return remotePath;
		} else {
			return remotePath.substring(lastFileSepPos + 1);
		}
	}

	public String getParent() {
		String fileSep = sshHostSession.getHostOperatingSystem().getFileSeparator();
		int lastFileSepPos = remotePath.lastIndexOf(fileSep);
		if (lastFileSepPos < 0 || remotePath.equals(fileSep)) {
			return "";
		} else if (lastFileSepPos == 0) {
			// the parent of something in the root directory is the root
			// directory itself.
			return fileSep;
		} else {
			return remotePath.substring(0, lastFileSepPos);
		}
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

	protected int executeCommand(CommandExecutionCallbackHandler handler, String... command) {
		return sshHostSession.execute(handler, command);
	}

	protected abstract void deleteFile();

	protected abstract void deleteDirectory();

	public String toString() {
		return getPath() + " on " + sshHostSession;
	}

}

