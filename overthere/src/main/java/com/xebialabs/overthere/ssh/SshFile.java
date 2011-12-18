/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;

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
		if(connection.getHostOperatingSystem() != WINDOWS) {
			this.path = path;
		} else {
			this.path = path.replace('/', '\\');
		}
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isHidden() {
		return getName().startsWith(".");
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
		String p = getPath();
		if(p.length() >= 1 && p.charAt(0) == '/') {
			return getConnection() + p;
		} else {
			return getConnection() + "/" + p;
		}
	}

}

