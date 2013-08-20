/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.OpenMode;
import net.schmizz.sshj.sftp.RemoteFile;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.xfer.FilePermission;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

/**
 * A file on a host connected through SSH that is accessed using SFTP.
 */
class SshSftpFile extends SshFile<SshSftpConnection> {

    public SshSftpFile(SshSftpConnection connection, String path) {
        super(connection, path);
    }

    protected String getSftpPath() {
        return connection.pathToSftpPath(getPath());
    }

    @Override
    public boolean exists() {
        logger.debug("Checking file " + this + " for existence");

        try {
            return connection.getSharedSftpClient().statExistence(getSftpPath()) != null;
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot check existence of file " + getPath(), e);
        }
    }

    @Override
    public boolean isFile() {
        return stat().getType() == FileMode.Type.REGULAR;
    }

    @Override
    public boolean isDirectory() {
        return stat().getType() == FileMode.Type.DIRECTORY;
    }

    @Override
    public long lastModified() {
        return stat().getMtime();
    }

    @Override
    public long length() {
        return stat().getSize();
    }

    @Override
    public boolean canRead() {
        return hasPermission(FilePermission.USR_R);
    }

    @Override
    public boolean canWrite() {
        return hasPermission(FilePermission.USR_W);
    }

    @Override
    public boolean canExecute() {
        return hasPermission(FilePermission.USR_X);
    }

    private boolean hasPermission(FilePermission perm) {
        return stat().getPermissions().contains(perm);
    }

    protected FileAttributes stat() {
        logger.debug("Statting file " + this);

        try {
            return connection.getSharedSftpClient().stat(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot stat file " + this, e);
        }
    }

    @Override
    public List<OverthereFile> listFiles() {
        logger.debug("Listing files in {}", this);

        try {
            // read files from host
            List<RemoteResourceInfo> ls = connection.getSharedSftpClient().ls(getSftpPath());

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
    public void mkdir() {
        logger.debug("Creating directory " + this);

        try {
            connection.getSharedSftpClient().mkdir(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot create directory " + this, e);
        }
    }

    @Override
    public void mkdirs() {
        logger.debug("Creating directories {}", this);
        try {
            connection.getSharedSftpClient().mkdirs(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot create directories " + this, e);
        }
    }

    @Override
    public void renameTo(OverthereFile dest) {
        logger.debug("Renaming {} to {}", this, dest);

        if (dest instanceof SshSftpFile) {
            SshSftpFile sftpDest = (SshSftpFile) dest;
            if (sftpDest.getConnection() == getConnection()) {
                try {
                    connection.getSharedSftpClient().rename(getSftpPath(), sftpDest.getSftpPath());
                } catch (IOException e) {
                    throw new RuntimeIOException("Cannot move/rename file/directory " + this + " to " + dest, e);
                }
            } else {
                throw new RuntimeIOException("Cannot move/rename ssh:" + connection.sshConnectionType.toString().toLowerCase() + ": file/directory " + this
                    + " to file/directory "
                    + dest + " because it is in a different connection");
            }
        } else {
            throw new RuntimeIOException("Cannot move/rename ssh:" + connection.sshConnectionType.toString().toLowerCase() + ": file/directory " + this
                + " to non-ssh:"
                + connection.sshConnectionType.toString().toLowerCase() + ": file/directory " + dest);
        }
    }

    @Override
    public void setExecutable(boolean executable) {
        logger.debug("Setting execute permission on {} to {}", this, executable);

        try {
            int permissionsMask = connection.getSharedSftpClient().stat(getSftpPath()).getMode().getPermissionsMask();
            if (executable) {
                permissionsMask |= 0111;
            } else {
                permissionsMask &= ~0111;
            }
            connection.getSharedSftpClient().chmod(getPath(), permissionsMask);
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot delete file " + this, e);
        }
    }

    @Override
    protected void deleteFile() {
        logger.debug("Removing file {}", this);

        try {
            connection.getSharedSftpClient().rm(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot delete file " + this, e);
        }
    }

    @Override
    protected void deleteDirectory() {
        logger.debug("Removing directory {}", this);

        try {
            connection.getSharedSftpClient().rmdir(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot delete directory " + this, e);
        }
    }

    @Override
    public InputStream getInputStream() {
        logger.debug("Opening SFTP input stream to read from file {}", this);

        try {
            final RemoteFile remoteFile = connection.getSharedSftpClient().open(getSftpPath(), newHashSet(OpenMode.READ));
            final RemoteFile.RemoteFileInputStream stream = remoteFile.getInputStream();
            return new InputStream() {

                @Override
                public int read() throws IOException {
                    return stream.read();
                }

                @Override
                public int read(byte[] b) throws IOException {
                    return stream.read(b);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return stream.read(b, off, len);
                }

                @Override
                public long skip(long n) throws IOException {
                    return stream.skip(n);
                }

                @Override
                public int available() throws IOException {
                    return stream.available();
                }

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        Closeables.closeQuietly(remoteFile);
                    }
                }

                @Override
                public void mark(int readlimit) {
                    stream.mark(readlimit);
                }

                @Override
                public void reset() throws IOException {
                    stream.reset();
                }

                @Override
                public boolean markSupported() {
                    return stream.markSupported();
                }
            };
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot read from file " + this, e);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        logger.debug("Opening SFTP ouput stream to write to file {}", this);

        try {
            final RemoteFile remoteFile = connection.getSharedSftpClient().open(getSftpPath(), newHashSet(OpenMode.CREAT, OpenMode.WRITE, OpenMode.TRUNC));
            final OutputStream wrapped = remoteFile.getOutputStream();

            return new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    wrapped.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    wrapped.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    wrapped.write(b, off, len);
                }

                @Override
                public void flush() throws IOException {
                    wrapped.flush();
                }

                @Override
                public void close() throws IOException {
                    try {
                        wrapped.close();
                    } finally {
                        Closeables.closeQuietly(remoteFile);
                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot write to file " + this, e);
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SshSftpFile.class);

}
