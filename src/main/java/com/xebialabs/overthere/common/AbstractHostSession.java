/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.overthere.common;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.AbstractHostSessionSpecification;
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.HostSession;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Abstract base class with common methods used by actual implementations of {@link HostSession}.
 */
public abstract class AbstractHostSession implements HostSession {

	private OperatingSystemFamily os;

	private String temporaryDirectoryPath;

	private HostFile sessionTemporaryDirectory;

	public static final long MAX_TEMP_RETRIES = 100;

	public AbstractHostSession(AbstractHostSessionSpecification spec) {
		this.os = spec.getOs();
		this.temporaryDirectoryPath = spec.getTemporaryDirectoryPath();
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
					logger.info("Created session temporary directory " + sessionTemporaryDirectory);
					return sessionTemporaryDirectory;
				}
				infix = "-" + Long.toString(Math.abs(r.nextLong()));
			}
			throw new RuntimeIOException("Cannot create session temporary directory on " + this);
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
				logger.info("Removed session temporary directory " + sessionTemporaryDirectory);
			} catch (RuntimeException exc) {
				logger.warn("Got exception while removing session temporary directory " + sessionTemporaryDirectory, exc);
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

	private static Logger logger = LoggerFactory.getLogger(AbstractHostSession.class);

}