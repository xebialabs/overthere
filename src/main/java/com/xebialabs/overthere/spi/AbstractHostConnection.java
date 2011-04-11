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
package com.xebialabs.overthere.spi;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.HostConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Abstract base class with common methods used by actual implementations of {@link com.xebialabs.overthere.HostConnection}.
 */
public abstract class AbstractHostConnection implements HostConnection {

	protected OperatingSystemFamily os;

	private String temporaryDirectoryPath;

	private OverthereFile sessionTemporaryDirectory;

	public static final long MAX_TEMP_RETRIES = 100;

	protected AbstractHostConnection(String type, OperatingSystemFamily os, ConnectionOptions options) {
		this.os = os;
		this.temporaryDirectoryPath = options.get("temporaryDirectoryPath", os.getDefaultTemporaryDirectoryPath());
	}
	
	protected AbstractHostConnection(String type, ConnectionOptions options) {
		this(type, options.<OperatingSystemFamily>get("os"), options);
	}

	public OperatingSystemFamily getHostOperatingSystem() {
		return os;
	}

	public String encodeCommandLineForExecution(String... cmdarray) {
		return os.encodeCommandLineForExecution(cmdarray);
	}

	public String encodeCommandLineForLogging(String... cmdarray) {
		return os.encodeCommandLineForLogging(cmdarray);
	}

	public void disconnect() {
		String doNotCleanUpTemporaryFiles = System.getProperty("overthere.donotcleanuptemporaryfiles");
		boolean doNotCleanUp = Boolean.valueOf(doNotCleanUpTemporaryFiles);
		if (!doNotCleanUp) {
			cleanupTemporaryFiles();
		}
	}

	protected synchronized OverthereFile getTemporaryDirectory() throws RuntimeIOException {
		if (sessionTemporaryDirectory == null) {
			OverthereFile temporaryDirectory = getFile(temporaryDirectoryPath);
			Random r = new Random();
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
			String prefix = "deployit-" + dateFormat.format(new Date());
			String infix = "";
			String suffix = ".tmp";
			for (int i = 0; i < MAX_TEMP_RETRIES; i++) {
				OverthereFile tempDir = createSessionTempDirectory(temporaryDirectory, prefix + infix + suffix);
				if (tempDir != null) {
					sessionTemporaryDirectory = tempDir;
					logger.info("Created connection temporary directory " + sessionTemporaryDirectory);
					return sessionTemporaryDirectory;
				}
				infix = "-" + Long.toString(Math.abs(r.nextLong()));
			}
			throw new RuntimeIOException("Cannot create connection temporary directory on " + this);
		}
		return sessionTemporaryDirectory;
	}

	protected OverthereFile createSessionTempDirectory(OverthereFile systemTempDirectory, String name) {
		OverthereFile f = getFile(systemTempDirectory, name);
		if (!f.exists()) {
			f.mkdir();
			return f;
		}
		return null;
	}

	public void cleanupTemporaryFiles() {
		if (sessionTemporaryDirectory != null) {
			try {
				sessionTemporaryDirectory.deleteRecursively();
				logger.info("Removed connection temporary directory " + sessionTemporaryDirectory);
			} catch (RuntimeException exc) {
				logger.warn("Got exception while removing connection temporary directory " + sessionTemporaryDirectory, exc);
			}
		}
	}

	public OverthereFile getTempFile(String nameTemplate) throws RuntimeIOException {
		String prefix, suffix;

		if (nameTemplate != null) {
			int pos = nameTemplate.lastIndexOf('/');
			if (pos != -1) {
				nameTemplate = nameTemplate.substring(pos + 1);
			}
			pos = nameTemplate.lastIndexOf('\\');
			if (pos != -1) {
				nameTemplate = nameTemplate.substring(pos + 1);
			}
		}

		if (isBlank(nameTemplate)) {
			prefix = "hostsession";
			suffix = ".tmp";
		} else {
			prefix = getBaseName(nameTemplate);
			suffix = "." + getExtension(nameTemplate);
		}

		return getTempFile(prefix, suffix);
	}

	public OverthereFile copyToTemporaryFile(File localFile) throws RuntimeIOException {
		OverthereFile t = getTempFile(localFile.getName());
		t.put(localFile);
		return t;
	}

	private static Logger logger = LoggerFactory.getLogger(AbstractHostConnection.class);

}

