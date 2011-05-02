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
package com.xebialabs.overthere.ssh;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.RuntimeIOException;
import org.slf4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.xebialabs.overthere.OverthereFile;
import org.slf4j.LoggerFactory;

/**
 * A file on a host connected through SSH that is accessed using SFTP.
 */
@SuppressWarnings("serial")
class SshSftpOverthereFile extends SshOverthereFile {

	private SshSftpHostConnection sshSftpHostConnection;

	public SshSftpOverthereFile(SshSftpHostConnection connection, String path) {
		super(connection, path);
		sshSftpHostConnection = connection;
	}

	protected SftpATTRS stat() throws RuntimeIOException {
		try {
			SftpATTRS attrs = sshSftpHostConnection.getSharedSftpChannel().stat(convertWindowsPathToWinSshdPath(getPath()));
			if (logger.isDebugEnabled())
				logger.debug("Statted file " + this);
			return attrs;
		} catch (SftpException exc) {
			throw new RuntimeIOException("Cannot stat file " + this, exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot stat file " + this, exc);
		}
	}

	public boolean exists() throws RuntimeIOException {
		try {
			sshSftpHostConnection.getSharedSftpChannel().stat(convertWindowsPathToWinSshdPath(getPath()));
			if (logger.isDebugEnabled())
				logger.debug("Checked file " + getPath() + " for existence and found it");
			return true;
		} catch (SftpException exc) {
			// if we get an SftpException while trying to stat the file, we
			// assume it does not exist
			if (logger.isDebugEnabled())
				logger.debug("Checked file " + getPath() + " for existence and did not find it");
			return false;
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot check existence of file " + getPath(), exc);
		}
	}

	public boolean isDirectory() throws RuntimeIOException {
		return stat().isDir();
	}

	public long length() throws RuntimeIOException {
		return stat().getSize();
	}

	public boolean canExecute() throws RuntimeIOException {
		SftpATTRS attrs = stat();
		return (attrs.getPermissions() & 0100) != 0;
	}

	public boolean canRead() throws RuntimeIOException {
		SftpATTRS attrs = stat();
		return (attrs.getPermissions() & 0400) != 0;
	}

	public boolean canWrite() throws RuntimeIOException {
		SftpATTRS attrs = stat();
		return (attrs.getPermissions() & 0200) != 0;
	}

	public String[] list() throws RuntimeIOException {
		try {
			// read files from host
			@SuppressWarnings("unchecked")
			Vector<LsEntry> ls = (Vector<LsEntry>) sshSftpHostConnection.getSharedSftpChannel().ls(convertWindowsPathToWinSshdPath(getPath()));

			// copy files to list, skipping . and ..
			List<String> filenames = new ArrayList<String>(ls.size());
			for (LsEntry lsEntry : ls) {
				String filename = lsEntry.getFilename();
				if (filename.equals(".") || filename.equals("..")) {
					continue;
				}
				filenames.add(filename);
			}
			return filenames.toArray(new String[filenames.size()]);
		} catch (SftpException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean mkdir() throws RuntimeIOException {
		try {
			String compatibleRemotePath = convertWindowsPathToWinSshdPath(getPath());
			sshSftpHostConnection.getSharedSftpChannel().mkdir(compatibleRemotePath);
			if (logger.isDebugEnabled())
				logger.debug("Created directory " + getPath());
			return true;
		} catch (SftpException exc) {
			throw new RuntimeIOException("Cannot create directory " + getPath() + ": " + exc.toString(), exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot create directory " + getPath() + ": " + exc.toString(), exc);
		}
	}

	@Override
	public boolean mkdirs() throws RuntimeIOException {
		List<OverthereFile> allDirs = new ArrayList<OverthereFile>();
		OverthereFile dir = this;
		do {
			allDirs.add(0, dir);
		} while ((dir = dir.getParentFile()) != null);

		for (OverthereFile each : allDirs) {
			if (!each.exists()) {
				if (!each.mkdir()) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean renameTo(File dest) {
		if (dest instanceof SshSftpOverthereFile) {
			SshSftpOverthereFile sftpDest = (SshSftpOverthereFile) dest;
			if (sftpDest.getConnection() == getConnection()) {
				try {
					sshSftpHostConnection.getSharedSftpChannel().rename(getPath(), sftpDest.getPath());
					return true;
				} catch (SftpException exc) {
					throw new RuntimeIOException("Cannot move/rename file/directory " + this + " to " + dest + ": " + exc.toString(), exc);
				} catch (JSchException exc) {
					throw new RuntimeIOException("Cannot move/rename file/directory " + this + " to " + dest + ": " + exc.toString(), exc);
				}
			} else {
				throw new RuntimeIOException("Cannot move/rename SSH/SCP file/directory " + this + " to SSH/SCP file/directory " + dest
				        + " because it is in a different connection");
			}
		} else {
			throw new RuntimeIOException("Cannot move/rename SSH/SCP file/directory " + this + " to non-SSH/SCP file/directory " + dest);
		}
	}

	@Override
	protected void deleteFile() {
		try {
			sshSftpHostConnection.getSharedSftpChannel().rm(convertWindowsPathToWinSshdPath(getPath()));
			if (logger.isDebugEnabled())
				logger.debug("Removed file " + this);
		} catch (SftpException exc) {
			throw new RuntimeIOException("Cannot delete file " + this + ": " + exc.toString(), exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot delete file " + this + ": " + exc.toString(), exc);
		}
	}

	@Override
	protected void deleteDirectory() {
		try {
			sshSftpHostConnection.getSharedSftpChannel().rmdir(convertWindowsPathToWinSshdPath(getPath()));
			if (logger.isDebugEnabled())
				logger.debug("Removed directory " + this);
		} catch (SftpException exc) {
			throw new RuntimeIOException("Cannot delete directory " + this + ": " + exc.toString(), exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot delete directory " + this + ": " + exc.toString(), exc);
		}
	}

	public InputStream get() throws RuntimeIOException {
		try {
			ChannelSftp sftpChannel = sshSftpHostConnection.openSftpChannel();
			InputStream in = new SshSftpInputStream(sshSftpHostConnection, sftpChannel, sftpChannel.get(convertWindowsPathToWinSshdPath(getPath())));
			if (logger.isDebugEnabled())
				logger.debug("Opened SFTP input stream to read from file " + this);
			return in;
		} catch (SftpException exc) {
			throw new RuntimeIOException("Cannot read from file " + getPath() + ": " + exc.toString(), exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot read from file " + getPath() + ": " + exc.toString(), exc);
		}
	}

	public void get(OutputStream out) throws RuntimeIOException {
		try {
			sshSftpHostConnection.getSharedSftpChannel().get(convertWindowsPathToWinSshdPath(getPath()), out);
			if (logger.isDebugEnabled())
				logger.debug("Wrote output stream from file " + this);
		} catch (SftpException exc) {
			throw new RuntimeIOException("Cannot read from file " + getPath() + ": " + exc.toString(), exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot read from file " + getPath() + ": " + exc.toString(), exc);
		}
	}

	public OutputStream put(long length) throws RuntimeIOException {
		try {
			ChannelSftp sftpChannel = sshSftpHostConnection.openSftpChannel();
			OutputStream out = new SshSftpOutputStream(sshSftpHostConnection, sftpChannel, sftpChannel.put(convertWindowsPathToWinSshdPath(getPath())));
			if (logger.isDebugEnabled())
				logger.debug("Opened SFTP ouput stream to write to file " + this);
			return out;
		} catch (SftpException exc) {
			throw new RuntimeIOException("Cannot write to file " + getPath() + ": " + exc.toString(), exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot write to file " + getPath() + ": " + exc.toString(), exc);
		}
	}

	public void put(InputStream in, long length) throws RuntimeIOException {
		try {
			sshSftpHostConnection.getSharedSftpChannel().put(in, convertWindowsPathToWinSshdPath(getPath()));
			if (logger.isDebugEnabled())
				logger.debug("Wrote input stream to file " + this);
		} catch (SftpException exc) {
			throw new RuntimeIOException("Cannot write to file " + getPath() + ": " + exc.toString(), exc);
		} catch (JSchException exc) {
			throw new RuntimeIOException("Cannot write to file " + getPath() + ": " + exc.toString(), exc);
		}
	}

	/**
	 * TODO: Do we still want to support WinSSHD? What about copssh?
	 */
	private String convertWindowsPathToWinSshdPath(String path) {
		if (getConnection().getHostOperatingSystem() == OperatingSystemFamily.WINDOWS) {
			String winSshdPath;
			if (path.length() == 2 && path.charAt(1) == ':') {
				winSshdPath = "/" + path.charAt(0);
			} else if (path.length() > 2 && path.charAt(1) == ':' && path.charAt(2) == '\\') {
				winSshdPath = "/" + path.replace('\\', '/').replace(":", "");
			} else {
				winSshdPath = path;
			}
			if (logger.isDebugEnabled())
				logger.debug("Translated Windows path " + path + " to WinSSHD path " + winSshdPath);
			path = winSshdPath;
		}
		return path;
	}

	private static Logger logger = LoggerFactory.getLogger(SshSftpOverthereFile.class);

}
