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

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.xfer.FilePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * A file on a host connected through SSH that is accessed using SFTP.
 */
class SshSftpOverthereFile extends SshOverthereFile<SshSftpOverthereConnection> {

	public SshSftpOverthereFile(SshSftpOverthereConnection connection, String path) {
		super(connection, path);
	}

	protected FileAttributes stat() throws RuntimeIOException {
		logger.info("Statting file " + this);

		try {
            return connection.getSharedSftpClient().stat(getPath());
		} catch (IOException e) {
            throw new RuntimeIOException("Cannot stat file " + this, e);
        }
    }

	public boolean exists() throws RuntimeIOException {
		logger.info("Checking file " + getPath() + " for existence");

		try {
            return connection.getSharedSftpClient().statExistence(getPath()) != null;
        } catch (IOException e) {
			throw new RuntimeIOException("Cannot check existence of file " + getPath(), e);
		}
	}

	@Override
	public boolean isDirectory() throws RuntimeIOException {
		return stat().getType() == FileMode.Type.DIRECTORY;
	}

	@Override
	public long lastModified() {
		return stat().getMtime();
	}

	@Override
	public long length() throws RuntimeIOException {
		return stat().getSize();
	}

    @Override
	public boolean canRead() throws RuntimeIOException {
        return hasPermission(FilePermission.USR_R);
	}

	@Override
	public boolean canWrite() throws RuntimeIOException {
        return hasPermission(FilePermission.USR_W);
	}

    @Override
    public boolean canExecute() throws RuntimeIOException {
        return hasPermission(FilePermission.USR_X);
    }

    private boolean hasPermission(FilePermission perm) {
        return stat().getPermissions().contains(perm);
    }

	@Override
	public List<OverthereFile> listFiles() {
		logger.debug("Listing files in {}", this);

		try {
			// read files from host
            List<RemoteResourceInfo> ls = connection.getSharedSftpClient().ls(getPath());

            // copy files to list, skipping . and ..
			List<OverthereFile> files = newArrayList();
            for (RemoteResourceInfo l : ls) {
				String filename = l.getName();
				if (filename.equals(".") || filename.equals("..")) {
					continue;
				}
				files.add(getFile(filename));
			}

			return files;
        } catch (IOException e) {
			throw new RuntimeIOException("Cannot list directory " + this, e);
		}
	}

	@Override
	public void mkdir() throws RuntimeIOException {
		logger.info("Creating directory " + this);

		try {
            connection.getSharedSftpClient().mkdir(getPath());
		} catch (IOException e) {
            throw new RuntimeIOException("Cannot create directory " + getPath(), e);
        }
    }

	@Override
	public void mkdirs() throws RuntimeIOException {
		logger.debug("Creating directories {}", this);
        try {
            connection.getSharedSftpClient().mkdirs(getPath());
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot create directories " + getPath(), e);
        }
	}

	@Override
	public void renameTo(OverthereFile dest) {
		logger.debug("Renaming {} to {}", this, dest);

		if (dest instanceof SshSftpOverthereFile) {
			SshSftpOverthereFile sftpDest = (SshSftpOverthereFile) dest;
			if (sftpDest.getConnection() == getConnection()) {
				try {
					connection.getSharedSftpClient().rename(getPath(), sftpDest.getPath());
				} catch (IOException e) {
                    throw new RuntimeIOException("Cannot move/rename file/directory " + this + " to " + dest, e);
                }
            } else {
				throw new RuntimeIOException("Cannot move/rename SSH/SFTP file/directory " + this + " to SSH/SFTP file/directory " + dest
				        + " because it is in a different connection");
			}
		} else {
			throw new RuntimeIOException("Cannot move/rename SSH/SFTP file/directory " + this + " to non-SSH/SFTP file/directory " + dest);
		}
	}

	@Override
	protected void deleteFile() {
		logger.debug("Removing file {}", this);

		try {
            connection.getSharedSftpClient().rm(getPath());
		} catch (IOException e) {
            throw new RuntimeIOException("Cannot delete file " + this, e);
        }
    }

	@Override
	protected void deleteDirectory() {
		logger.debug("Removing directory {}", this);

		try {
            connection.getSharedSftpClient().rmdir(getPath());
		} catch (IOException e) {
            throw new RuntimeIOException("Cannot delete directory " + this, e);
        }
    }

	@Override
	public InputStream getInputStream() {
		logger.debug("Opening SFTP input stream to read from file {}", this);

        try {
            return connection.getSharedSftpClient().open(getPath(), newHashSet(OpenMode.READ)).getInputStream();
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot read from file " + getPath(), e);
        }
	}

	@Override
	public OutputStream getOutputStream(long length) throws RuntimeIOException {
		logger.debug("Opening SFTP ouput stream to write to file {}", this);


        try {
            return connection.getSharedSftpClient().open(getPath(), newHashSet(OpenMode.CREAT, OpenMode.WRITE)).getOutputStream();
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot write to file " + getPath(), e);
        }
	}

    private static Logger logger = LoggerFactory.getLogger(SshSftpOverthereFile.class);

}
