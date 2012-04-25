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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.BaseOverthereFile;

import static com.google.common.collect.Lists.newArrayList;

class CifsFile extends BaseOverthereFile<CifsConnection> {

	private SmbFile smbFile;

	protected CifsFile(CifsConnection connection, SmbFile smbFile) {
		super(connection);
		this.smbFile = smbFile;
	}

	protected SmbFile getSmbFile() {
		return smbFile;
	}

	@Override
	public String getPath() {
		return connection.encoder.fromUncPath(smbFile.getUncPath());
	}

	@Override
	public String getName() {
		return smbFile.getName();
	}

	@Override
	public OverthereFile getParentFile() {
		try {
			return new CifsFile(getConnection(), new SmbFile(smbFile.getParent(), connection.authentication));
		} catch (MalformedURLException exc) {
			return null;
		}
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
	public boolean canRead() throws RuntimeIOException {
		try {
			return smbFile.canRead();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " can be read: " + exc.toString(), exc);
		}
	}

	@Override
	public boolean canWrite() throws RuntimeIOException {
		try {
			return smbFile.canWrite();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " can be written: " + exc.toString(), exc);
		}
	}

	@Override
	public boolean canExecute() throws RuntimeIOException {
		try {
			return smbFile.canRead();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " can be executed: " + exc.toString(), exc);
		}
	}

	@Override
	public boolean isFile() throws RuntimeIOException {
		try {
			return smbFile.isFile();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " is a directory: " + exc.toString(), exc);
		}
	}

	@Override
	public boolean isDirectory() throws RuntimeIOException {
		try {
			return smbFile.isDirectory();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " is a directory: " + exc.toString(), exc);
		}
	}

	@Override
	public boolean isHidden() {
		try {
			return smbFile.isHidden();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine whether " + this + " is hidden: " + exc.toString(), exc);
		}
	}

	@Override
	public long lastModified() {
		try {
			return smbFile.lastModified();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot determine last modification timestamp of " + this + ": " + exc.toString(), exc);
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
	public List<OverthereFile> listFiles() throws RuntimeIOException {
		try {
			upgradeToDirectorySmbFile();
			List<OverthereFile> files = newArrayList();
			for (String name : smbFile.list()) {
				files.add(getFile(name));
			}
			return files;
		} catch (MalformedURLException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public void mkdir() throws RuntimeIOException {
		try {
			smbFile.mkdir();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot create directory " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public void mkdirs() throws RuntimeIOException {
		try {
			smbFile.mkdirs();
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot create directories " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public void renameTo(OverthereFile dest) throws RuntimeIOException {
		if (dest instanceof CifsFile) {
			SmbFile targetSmbFile = ((CifsFile) dest).getSmbFile();
			try {
				smbFile.renameTo(targetSmbFile);
			} catch (SmbException exc) {
				throw new RuntimeIOException("Cannot move/rename " + this + " to " + dest + ": " + exc.toString(), exc);
			}
		} else {
			throw new RuntimeIOException("Cannot move/rename cifs:" + connection.cifsConnectionType.toString().toLowerCase() + ": file/directory " + this + " to non-cifs:" + connection.cifsConnectionType.toString().toLowerCase() + ": file/directory " + dest);
		}
	}

	@Override
    public void setExecutable(boolean executable) {
		// the execute permission does not exist on Windows
    }

	@Override
	public void delete() throws RuntimeIOException {
		try {
			if (smbFile.isDirectory()) {
				upgradeToDirectorySmbFile();
				if (smbFile.list().length > 0) {
					throw new RuntimeIOException("Cannot delete non-empty directory " + this);
				}
			}
			smbFile.delete();
			refreshSmbFile();
		} catch (MalformedURLException exc) {
			throw new RuntimeIOException("Cannot delete " + this + ": " + exc.toString(), exc);
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot delete " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public void deleteRecursively() throws RuntimeIOException {
		try {
			if (smbFile.isDirectory()) {
				upgradeToDirectorySmbFile();
			}
			smbFile.delete();
			refreshSmbFile();
		} catch (MalformedURLException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		} catch (SmbException exc) {
			throw new RuntimeIOException("Cannot delete " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public InputStream getInputStream() throws RuntimeIOException {
		try {
			return smbFile.getInputStream();
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot open " + this + " for reading: " + exc.toString(), exc);
		}
	}

	@Override
	public OutputStream getOutputStream() {
		try {
			return smbFile.getOutputStream();
		} catch (IOException exc) {
			throw new RuntimeIOException("Cannot open " + this + " for writing: " + exc.toString(), exc);
		}
	}

	private void upgradeToDirectorySmbFile() throws MalformedURLException {
		if (!smbFile.getPath().endsWith("/")) {
			smbFile = new SmbFile(smbFile.getURL() + "/", connection.authentication);
		}
	}

	private void refreshSmbFile() throws MalformedURLException {
		smbFile = new SmbFile(smbFile.getPath(), connection.authentication);
	}

	@Override
	public boolean equals(Object that) {
		if (!(that instanceof CifsFile)) {
			return false;
		}

		return getPath().equals(((CifsFile) that).getPath());
	}

	@Override
	public int hashCode() {
		return smbFile.getPath().hashCode();
	}

	@Override
	public String toString() {
		return getConnection() + "/" + getPath();
	}

}
