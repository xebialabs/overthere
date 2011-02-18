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

package com.xebialabs.overthere.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.HostSession;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.common.AbstractHostFile;


/**
 * A local host file
 */
class LocalHostFile extends AbstractHostFile implements HostFile {

	private File file;

	public LocalHostFile(HostSession session, File file) {
		super(session);
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public String getPath() {
		return file.getPath();
	}

	public String getName() {
		return file.getName();
	}

	public String getParent() {
		return file.getParent();
	}

	public boolean exists() {
		return file.exists();
	}

	public boolean isDirectory() {
		return file.isDirectory();
	}

	public long length() throws RuntimeIOException {
		return file.length();
	}

	public boolean canRead() {
		return file.canRead();
	}

	public boolean canWrite() {
		return file.canWrite();
	}

	public boolean canExecute() {
		if (session.getHostOperatingSystem() == OperatingSystemFamily.UNIX) {
			return executeStat().canExecute;
		} else {
			return canRead();
		}
	}

	public List<String> list() throws RuntimeIOException {
		String[] listArray = file.list();
		if (listArray == null) {
			return null;
		} else {
			return Arrays.asList(listArray);
		}
	}

	public void mkdir() throws RuntimeIOException {
		if (!file.mkdir()) {
			throw new RuntimeIOException("Cannot create directory " + file.getPath());
		}
	}

	public void mkdirs() throws RuntimeIOException {
		if (file.exists()) {
			return;
		}
		if (!file.mkdirs()) {
			throw new RuntimeIOException("Cannot create directory " + file.getPath() + " or one of its parent directories");
		}
	}

	public void moveTo(HostFile destFile) throws RuntimeIOException {
		if (destFile instanceof LocalHostFile) {
			File destLocalFile = ((LocalHostFile) destFile).getFile();
			if (!file.renameTo(destLocalFile)) {
				throw new RuntimeIOException("Cannot move/rename " + this + " to " + destFile);
			}
		} else {
			throw new RuntimeIOException("Cannot move/rename local file/directory " + this + " to non-local file/directory " + destFile);
		}
	}

	public boolean delete() {
		if (file.exists()) {
			if (!file.delete()) {
				throw new RuntimeIOException("Cannot delete " + file.getPath());
			}
			return true;
		} else {
			return false;
		}
	}

	public InputStream get() throws RuntimeIOException {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public OutputStream put(long length) throws RuntimeIOException {
		try {
			return new FileOutputStream(file);
		} catch (FileNotFoundException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	public String toString() {
		return file.toString();
	}

	@Override
	protected int executeCommand(CommandExecutionCallbackHandler handler, String... command) {
		return session.execute(handler, command);
	}
	
}
