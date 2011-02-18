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

package com.xebialabs.overthere.cifs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import com.xebialabs.deployit.exception.RuntimeIOException;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.HostSession;
import com.xebialabs.overthere.common.AbstractHostFile;

public class CifsHostFile extends AbstractHostFile implements HostFile {

	private SmbFile smbFile;

	CifsHostFile(HostSession session, SmbFile smbFile) {
		super(session);
		this.smbFile = smbFile;
	}

	SmbFile getSmbFile() {
		return smbFile;
	}

	public String getName() {
		return smbFile.getName();
	}

	public String getParent() {
		return smbFile.getParent();
	}

	public String getPath() {
		String uncPath = smbFile.getUncPath();
		int slashPos = uncPath.indexOf('\\', 2);
		String drive = uncPath.substring(slashPos + 1, slashPos + 2);
		String path = uncPath.substring(slashPos + 3);
		return drive + ":" + path;
	}

	public boolean exists() throws RuntimeIOException {
		try {
			return smbFile.exists();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine existence of " + this + ": " + exc.toString(), exc);
		}
	}

	public boolean isDirectory() throws RuntimeIOException {
		try {
			return smbFile.isDirectory();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " is a directory" + ": " + exc.toString(), exc);
		}
	}

	public boolean canExecute() throws RuntimeIOException {
		try {
			return smbFile.canRead();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " can be executed" + ": " + exc.toString(), exc);
		}
	}

	public boolean canRead() throws RuntimeIOException {
		try {
			return smbFile.canRead();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " can be read" + ": " + exc.toString(), exc);
		}
	}

	public boolean canWrite() throws RuntimeIOException {
		try {
			return smbFile.canWrite();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " can be written" + ": " + exc.toString(), exc);
		}
	}

	public long length() throws RuntimeIOException {
		try {
			return smbFile.length();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine length of file " + this + ": " + exc.toString(), exc);
		}
	}

	public List<String> list() throws RuntimeIOException {
		try {
			upgradeToDirectorySmbFile();
			return Arrays.asList(smbFile.list());
		} catch (MalformedURLException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		}
	}

	public void mkdir() throws RuntimeIOException {
		try {
			smbFile.mkdir();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot create directory " + this + ": " + exc.toString(), exc);
		}
	}

	public void mkdirs() throws RuntimeIOException {
		try {
			smbFile.mkdirs();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot create directories " + this + ": " + exc.toString(), exc);
		}
	}

	public void moveTo(HostFile destFile) throws RuntimeIOException {
		if (destFile instanceof CifsHostFile) {
			SmbFile targetSmbFile = ((CifsHostFile) destFile).getSmbFile();
			try {
				smbFile.renameTo(targetSmbFile);
			} catch (SmbException exc) {
				throw new RuntimeIOException("Cannot move/rename " + this + " to " + destFile + ": " + exc.toString(), exc);
			}
		} else {
			throw new RuntimeIOException("Cannot move/rename SMB file/directory " + this + " to non-SMB file/directory " + destFile);
		}
	}

	public boolean delete() throws RuntimeIOException {
		try {
			if (smbFile.exists()) {
				if (smbFile.isDirectory()) {
					upgradeToDirectorySmbFile();
					String[] files = smbFile.list();
					if (files.length != 0) {
						throw new RuntimeIOException("Cannot delete non-empty directory " + this);
					}
				}
				smbFile.delete();
				refreshSmbFile();
				return true;
			} else {
				return false;
			}
		} catch (MalformedURLException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot delete " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean deleteRecursively() throws RuntimeIOException {
		try {
			if (smbFile.exists()) {
				if (smbFile.isDirectory()) {
					upgradeToDirectorySmbFile();
				}
				smbFile.delete();
				refreshSmbFile();
				return true;
			} else {
				return true;
			}
		} catch (MalformedURLException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot delete " + this + ": " + exc.toString(), exc);
		}
	}

	public InputStream get() throws RuntimeIOException {
		try {
			return smbFile.getInputStream();
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot read from file " + this + ": " + exc.toString(), exc);
		}
	}

	public OutputStream put(long length) throws RuntimeIOException {
		try {
			return smbFile.getOutputStream();
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot write to file " + this + ": " + exc.toString(), exc);
		}
	}

	private void upgradeToDirectorySmbFile() throws MalformedURLException {
		if (!smbFile.getPath().endsWith("/")) {
			smbFile = new SmbFile(smbFile.getURL() + "/");
		}
	}

	private void refreshSmbFile() throws MalformedURLException {
		smbFile = new SmbFile(smbFile.getPath());
	}

	public String toString() {
		return getPath() + " on " + session;
	}
	
	@Override
	protected int executeCommand(CommandExecutionCallbackHandler handler, String... command) {
		return session.execute(handler, command);
	}


}
