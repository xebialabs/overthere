/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
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

package com.xebialabs.overthere.local;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.OperatingSystemFamily.getLocalHostOperatingSystemFamily;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;

/**
 * A connection to the local host.
 */
@Protocol(name = LOCAL_PROTOCOL)
public class LocalConnection extends BaseOverthereConnection implements OverthereConnectionBuilder {

	/**
	 * Name of the protocol handled by this connection builder, i.e. "local".
	 */
	public static final String LOCAL_PROTOCOL = "local";
	
	/**
	 * Constructs a connection to the local host.
	 */
	public LocalConnection(String protocol, ConnectionOptions options, AddressPortMapper mapper) {
		super(protocol, fixOptions(options), mapper, true);
	}

	/**
	 * Constructs a connection to the local host.
	 */
	public LocalConnection(String protocol, ConnectionOptions options) {
		this(protocol, options, new DefaultAddressPortMapper());
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
			final ProcessBuilder pb = new ProcessBuilder(commandLine.toCommandArray(os, false));
			if(workingDirectory != null) {
				pb.directory(((LocalFile) workingDirectory).getFile());
			}
			final Process p = pb.start();
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
			throw new RuntimeIOException("Cannot start process for " + commandLine, exc);
		}
	}

	@Override
	public String toString() {
		return LOCAL_PROTOCOL + ":";
	}

	/**
	 * Creates a connection to the local host.
	 */
	public static OverthereConnection getLocalConnection() {
		return Overthere.getConnection("local", new ConnectionOptions());
	}

	private static final Logger logger = LoggerFactory.getLogger(LocalConnection.class);

}

