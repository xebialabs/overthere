package com.xebialabs.overthere.common;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.xebialabs.overthere.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class with common methods used by actual implementations of {@link com.xebialabs.overthere.HostConnection}.
 */
public abstract class AbstractHostConnection implements HostConnection {

	private OperatingSystemFamily os;

	private String temporaryDirectoryPath;

	private HostFile sessionTemporaryDirectory;

	public static final long MAX_TEMP_RETRIES = 100;

	protected AbstractHostConnection(String type, ConnectionOptions options) {
		this.os = options.get("os");
		this.temporaryDirectoryPath = options.get("temporaryDirectoryPath");
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

	public void close() {
		String doNotCleanUpTemporaryFiles = System.getProperty("com.xebia.ad.donotcleanuptemporaryfiles");
		boolean doNotCleanUp = (doNotCleanUpTemporaryFiles != null && doNotCleanUpTemporaryFiles.equalsIgnoreCase("true"));
		if (!doNotCleanUp) {
			cleanupTemporaryFiles();
		}
	}

	protected synchronized HostFile getTemporaryDirectory() throws RuntimeIOException {
		if (sessionTemporaryDirectory == null) {
			HostFile temporaryDirectory = getFile(temporaryDirectoryPath);
			Random r = new Random();
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
			String prefix = "deployit-" + dateFormat.format(new Date());
			String infix = "";
			String suffix = ".tmp";
			for (int i = 0; i < MAX_TEMP_RETRIES; i++) {
				HostFile tempDir = createSessionTempDirectory(temporaryDirectory, prefix + infix + suffix);
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

	protected HostFile createSessionTempDirectory(HostFile systemTempDirectory, String name) {
		HostFile f = getFile(systemTempDirectory, name);
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

	public HostFile getTempFile(String nameTemplate) throws RuntimeIOException {
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

		if (StringUtils.isBlank(nameTemplate)) {
			prefix = "hostsession";
			suffix = ".tmp";
		} else {
			prefix = FilenameUtils.getBaseName(nameTemplate);
			suffix = "." + FilenameUtils.getExtension(nameTemplate);
		}

		return getTempFile(prefix, suffix);
	}

	public HostFile copyToTemporaryFile(File localFile) throws RuntimeIOException {
		HostFile t = getTempFile(localFile.getName());
		t.put(localFile);
		return t;
	}

	private static Logger logger = LoggerFactory.getLogger(AbstractHostConnection.class);

}