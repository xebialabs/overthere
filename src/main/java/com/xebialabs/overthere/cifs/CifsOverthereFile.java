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
package com.xebialabs.overthere.cifs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import com.xebialabs.overthere.HostConnection;
import com.xebialabs.overthere.RemoteOverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

@SuppressWarnings("serial")
public class CifsOverthereFile extends RemoteOverthereFile {

	private SmbFile smbFile;

	CifsOverthereFile(HostConnection connection, SmbFile smbFile) {
		super(connection, smbFile.getPath());
		this.smbFile = smbFile;
	}

	SmbFile getSmbFile() {
		return smbFile;
	}

	@Override
	public String getPath() {
		String uncPath = smbFile.getUncPath();
		int slashPos = uncPath.indexOf('\\', 2);
		String drive = uncPath.substring(slashPos + 1, slashPos + 2);
		String path = uncPath.substring(slashPos + 3);
		return drive + ":" + path;
	}

	@Override
	public String getName() {
		return smbFile.getName();
	}

	@Override
	public String getParent() {
		return smbFile.getParent();
	}

	@Override
	public boolean exists() throws RuntimeIOException {
		try {
			return smbFile.exists();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine existence of " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean isDirectory() throws RuntimeIOException {
		try {
			return smbFile.isDirectory();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " is a directory" + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean canExecute() throws RuntimeIOException {
		try {
			return smbFile.canRead();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " can be executed" + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean canRead() throws RuntimeIOException {
		try {
			return smbFile.canRead();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " can be read" + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean canWrite() throws RuntimeIOException {
		try {
			return smbFile.canWrite();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " can be written" + ": " + exc.toString(), exc);
		}
	}

	@Override
	public long length() throws RuntimeIOException {
		try {
			return smbFile.length();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine length of file " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public String[] list() throws RuntimeIOException {
		try {
			upgradeToDirectorySmbFile();
			return smbFile.list();
		} catch (MalformedURLException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean mkdir() throws RuntimeIOException {
		try {
			smbFile.mkdir();
			return true;
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot create directory " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean mkdirs() throws RuntimeIOException {
		try {
			smbFile.mkdirs();
			return true;
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot create directories " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean renameTo(File dest) throws RuntimeIOException {
		if (dest instanceof CifsOverthereFile) {
			SmbFile targetSmbFile = ((CifsOverthereFile) dest).getSmbFile();
			try {
				smbFile.renameTo(targetSmbFile);
				return true;
			} catch (SmbException exc) {
				throw new RuntimeIOException("Cannot move/rename " + this + " to " + dest + ": " + exc.toString(), exc);
			}
		} else {
			throw new RuntimeIOException("Cannot move/rename SMB file/directory " + this + " to non-SMB file/directory " + dest);
		}
	}

	@Override
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

	@Override
	public InputStream get() throws RuntimeIOException {
		try {
			return smbFile.getInputStream();
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot read from file " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
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
		return getPath() + " on " + connection;
	}

}
