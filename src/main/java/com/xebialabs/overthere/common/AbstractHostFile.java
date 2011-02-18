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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.HostSession;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Abstract base class with common methods used by actual implementations of {@link HostFile}.
 */
public abstract class AbstractHostFile implements HostFile {

	protected HostSession session;

	public AbstractHostFile(HostSession session) {
		this.session = session;
	}

	public HostSession getSession() {
		return session;
	}

	public HostFile getFile(String name) {
		return getSession().getFile(this, name);
	}

	public HostFile getParentFile() {
		String parent = getParent();
		if (parent == null || parent.length() == 0) {
			return null;
		} else {
			return getSession().getFile(parent);
		}
	}

	public List<HostFile> listFiles() throws RuntimeIOException {
		List<String> filenames = list();
		if (filenames == null) {
			return null;
		} else {
			List<HostFile> listFiles = new ArrayList<HostFile>(filenames.size());
			for (String filename : filenames) {
				listFiles.add(getSession().getFile(this, filename));
			}
			return listFiles;
		}
	}

	public boolean deleteRecursively() throws RuntimeIOException {
		if (!exists()) {
			return false;
		} else {
			deleteRecursivelyWithoutExistenceCheck(this);
			return true;
		}
	}

	private static void deleteRecursivelyWithoutExistenceCheck(HostFile d) throws RuntimeIOException {
		if (d.isDirectory()) {
			List<HostFile> contents = d.listFiles();
			for (HostFile f : contents) {
				deleteRecursivelyWithoutExistenceCheck(f);
			}
		}

		// The SSH implementation of delete actually redoes the existence check :-(
		d.delete();
	}

	public void get(OutputStream out) throws RuntimeIOException {
		try {
			InputStream in = get();
			try {
				IOUtils.copy(in, out);
			} finally {
				IOUtils.closeQuietly(in);
			}
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public void get(File file) throws RuntimeIOException {
		try {
			OutputStream out = new FileOutputStream(file);
			try {
				get(out);
			} finally {
				out.close();
			}
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public void put(InputStream in, long length) throws RuntimeIOException {
		try {
			OutputStream out = put(length);
			try {
				IOUtils.copy(in, out);
			} finally {
				IOUtils.closeQuietly(out);
			}
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public void put(File file) throws RuntimeIOException {
		try {
			InputStream in = new FileInputStream(file);
			try {
				put(in, file.length());
			} finally {
				in.close();
			}
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	/**
	 * Holds results of an ls call
	 */
	protected static class StatResults {
		public boolean exists;
		public boolean isDirectory;
		public long length = -1;

		public boolean canRead;
		public boolean canWrite;
		public boolean canExecute;
	}

	protected StatResults executeStat() throws RuntimeIOException {
		StatResults results = new StatResults();
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int errno = executeCommand(capturedOutput, "ls", "-ld", getPath());
		if (errno == 0) {
			results.exists = true;
			if (capturedOutput.getOutputLines().size() > 0) {
				// parse ls results
				String outputLine = capturedOutput.getOutputLines().get(0);
				if (logger.isDebugEnabled())
					logger.debug("ls output = " + outputLine);
				StringTokenizer outputTokens = new StringTokenizer(outputLine);
				if (outputTokens.countTokens() < 5) {
					throw new RuntimeIOException("ls -ld " + getPath() + " returned output that contains less than the expected 5 tokens");
				}
				String permissions = outputTokens.nextToken();
				@SuppressWarnings("unused")
				String inodelinkes = outputTokens.nextToken();
				@SuppressWarnings("unused")
				String owner = outputTokens.nextToken();
				@SuppressWarnings("unused")
				String group = outputTokens.nextToken();
				String size = outputTokens.nextToken();

				results.isDirectory = permissions.length() >= 1 && permissions.charAt(0) == 'd';
				results.canRead = permissions.length() >= 2 && permissions.charAt(1) == 'r';
				results.canWrite = permissions.length() >= 3 && permissions.charAt(2) == 'w';
				results.canExecute = permissions.length() >= 4 && permissions.charAt(3) == 'x';
				try {
					results.length = Integer.parseInt(size);
				} catch (NumberFormatException ignore) {
				}
			}
		} else {
			results.exists = false;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Statted file " + this + " (exists=" + results.exists + ", isDirectory=" + results.isDirectory + ", length=" + results.length
					+ ", canRead=" + results.canRead + ", canWrite=" + results.canWrite + ", canExecute=" + results.canExecute + ")");
		}
		return results;
	}

	protected abstract int executeCommand(CommandExecutionCallbackHandler handler, String... command);

	private static Logger logger = LoggerFactory.getLogger(AbstractHostFile.class);
}
