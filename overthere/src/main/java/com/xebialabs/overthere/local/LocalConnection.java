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

import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.OperatingSystemFamily.getLocalHostOperatingSystemFamily;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection to the local host.
 */
@Protocol(name = "local")
public class LocalConnection extends OverthereConnection implements OverthereConnectionBuilder {

	/**
	 * Constructs a connection to the local host.
	 */
	public LocalConnection(String type, ConnectionOptions options) {
		super(type, fixOptions(options), true);
	}

	private static ConnectionOptions fixOptions(ConnectionOptions options) {
		options = new ConnectionOptions(options);
		options.set(OPERATING_SYSTEM, getLocalHostOperatingSystemFamily());
		if(options.getOptional(TEMPORARY_DIRECTORY_PATH) == null) {
			options.set(TEMPORARY_DIRECTORY_PATH, System.getProperty("java.io.tmpdir"));
		}
	    return options;
    }

	@Override
	public OverthereConnection connect() {
		return this;
	}
	
	@Override
	public void doClose() {
		// no-op
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
    protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
	    return getFile(parent, name);
    }

	@Override
	public OverthereProcess startProcess(CmdLine commandLine) {
		logger.info("Executing command {} on {}", commandLine, this);
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

	/**
	 * Creates a connection to the local host.
	 */
	public static OverthereConnection getLocalConnection() {
		return Overthere.getConnection("local", new ConnectionOptions());
	}

	private static final Logger logger = LoggerFactory.getLogger(LocalConnection.class);
}
