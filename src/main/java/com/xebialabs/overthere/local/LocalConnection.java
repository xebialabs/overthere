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

import static com.xebialabs.overthere.OperatingSystemFamily.getLocalHostOperatingSystemFamily;
import static java.io.File.createTempFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

/**
 * A connection to the local host.
 */
@Protocol(name = "local")
public class LocalConnection extends OverthereConnection implements OverthereConnectionBuilder {

	/**
	 * Constructs a connection to the local host.
	 */
	public LocalConnection(String type, ConnectionOptions options) {
		super(type, getLocalHostOperatingSystemFamily(), options);
	}

	@Override
	public OverthereConnection connect() {
		return this;
	}
	
	@Override
	public void doDisconnect() {
		// no-op
	}

	@Override
	public OverthereFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
		try {
			File tempFile = createTempFile(prefix, suffix, new File(getTempDirectory().getPath()));
			// FIXME: Need to delete this file to make test work, but isn't it better to NOT do that so that a tempfile with the same name is not accidentally
			// created simultaneously?
			// FIXME: Answer from VP: we have to decide on the semantics. How do you create a temporary directory?
			tempFile.delete();
			return new LocalFile(this, tempFile);
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	@Override
	public OverthereFile getFile(String path) throws RuntimeIOException {
		return new LocalFile(this, new File(path));
	}

	@Override
	public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
		if (!(parent instanceof LocalFile)) {
			throw new IllegalStateException("parent is not a LocalOverthereFile");
		}

		File childFile = new File(((LocalFile) parent).getFile(), child);
		return new LocalFile(this, childFile);
	}

	@Override
	public OverthereProcess startProcess(CmdLine commandLine) {
		try {
			final Process p = Runtime.getRuntime().exec(commandLine.toCommandLine(getHostOperatingSystem(), false));
			return new OverthereProcess() {

				@Override
				public OutputStream getStdin() {
					return p.getOutputStream();
				}

				@Override
				public InputStream getStdout() {
					return p.getInputStream();
				}

				@Override
				public InputStream getStderr() {
					return p.getErrorStream();
				}

				@Override
				public int waitFor() throws InterruptedException {
					return p.waitFor();
				}

				@Override
				public void destroy() {
					p.destroy();
				}
			};
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot start process for " + commandLine);
		}
	}

	@Override
	public String toString() {
		return "localhost";
	}

}
