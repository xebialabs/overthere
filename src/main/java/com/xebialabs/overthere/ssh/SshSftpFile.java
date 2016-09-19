/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
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

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import net.schmizz.sshj.sftp.*;
import net.schmizz.sshj.xfer.FilePermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static java.lang.String.format;

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
        logger.debug("Checking {} for existence", this);

        try {
            return connection.getSharedSftpClient().statExistence(getSftpPath()) != null;
        } catch (IOException e) {
            throw new RuntimeIOException(format("Cannot check existence of file %s", this), e);
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
        return stat().getMtime() * 1000;
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
        logger.debug("Statting {}", this);

        try {
            return connection.getSharedSftpClient().stat(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException(format("Cannot stat %s", this), e);
        }
    }

    @Override
    public List<OverthereFile> listFiles() {
        logger.debug("Listing directory {}", this);

        try {
            // read files from host
            List<RemoteResourceInfo> ls = connection.getSharedSftpClient().ls(getSftpPath());

            // copy files to list, skipping . and ..
            List<OverthereFile> files = new ArrayList<OverthereFile>();
            for (RemoteResourceInfo l : ls) {
                String filename = l.getName();
                if (filename.equals(".") || filename.equals("..")) {
                    continue;
                }
                files.add(getFile(filename));
            }

            return files;
        } catch (IOException e) {
            throw new RuntimeIOException(format("Cannot list directory %s", this), e);
        }
    }

    @Override
    protected void copyFrom(OverthereFile source) {
        SFTPFileTransfer fileTransfer = connection.getSharedSftpClient().getFileTransfer();
        try {
            fileTransfer.upload(new OverthereFileLocalSourceFile(source), getSftpPath());
        } catch (IOException ioe) {
            throw new RuntimeIOException(format("Cannot upload %s to %s", source, this), ioe);
        }
    }

    @Override
    public void mkdir() {
        logger.debug("Creating directory {}", this);

        try {
            connection.getSharedSftpClient().mkdir(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException(format("Cannot create directory %s", this), e);
        }
    }

    @Override
    public void mkdirs() {
        logger.debug("Creating directories {}", this);
        try {
            connection.getSharedSftpClient().mkdirs(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException(format("Cannot create directories %s", this), e);
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
                    throw new RuntimeIOException(format("Cannot move/rename file/directory %s to %s", this, dest), e);
                }
            } else {
                throw new RuntimeIOException(format(
                        "Cannot move/rename %s file/directory %s to file/directory %s because it is in a different connection",
                        connection.protocolAndConnectionType, this, dest));
            }
        } else {
            throw new RuntimeIOException(format("Cannot move/rename %s file/directory %s to non-%s file/directory %s",
                    connection.protocolAndConnectionType, this, connection.protocolAndConnectionType, dest));
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
            throw new RuntimeIOException(format("Cannot set execute permission on %s to %b", this, executable), e);
        }
    }

    @Override
    protected void deleteFile() {
        logger.debug("Deleting file {}", this);

        try {
            connection.getSharedSftpClient().rm(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException(format("Cannot delete file %s", this), e);
        }
    }

    @Override
    protected void deleteDirectory() {
        logger.debug("Deleting directory {}", this);

        try {
            connection.getSharedSftpClient().rmdir(getSftpPath());
        } catch (IOException e) {
            throw new RuntimeIOException(format("Cannot delete directory %s", this), e);
        }
    }

    @Override
    public InputStream getInputStream() {
        logger.debug("Opening SFTP input stream for {}", this);

        try {
            final SFTPClient sftp = connection.connectSftp();
            final RemoteFile remoteFile = sftp.open(getSftpPath(), EnumSet.of(OpenMode.READ));
            final InputStream wrapped = remoteFile.new RemoteFileInputStream();

            return asBuffered(new InputStream() {

                @Override
                public int read() throws IOException {
                    return wrapped.read();
                }

                @Override
                public int read(byte[] b) throws IOException {
                    return wrapped.read(b);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return wrapped.read(b, off, len);
                }

                @Override
                public long skip(long n) throws IOException {
                    return wrapped.skip(n);
                }

                @Override
                public int available() throws IOException {
                    return wrapped.available();
                }

                @Override
                public boolean markSupported() {
                    return wrapped.markSupported();
                }

                @Override
                public void mark(int readlimit) {
                    wrapped.mark(readlimit);
                }

                @Override
                public void reset() throws IOException {
                    wrapped.reset();
                }

                @Override
                public void close() throws IOException {
                    logger.info("Closing SFTP input stream for {}", SshSftpFile.this);
                    try {
                        wrapped.close();
                    } finally {
                        closeQuietly(remoteFile);
                        connection.disconnectSftp(sftp);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot read from file " + this, e);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        logger.debug("Opening SFTP ouput stream for {}", this);

        try {
            final SFTPClient sftp = connection.connectSftp();
            final RemoteFile remoteFile = sftp.open(getSftpPath(), EnumSet.of(OpenMode.CREAT, OpenMode.WRITE, OpenMode.TRUNC));
            final OutputStream wrapped = remoteFile.new RemoteFileOutputStream();

            return asBuffered(new OutputStream() {

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
                    logger.info("Closing SFTP output stream for {}", SshSftpFile.this);
                    try {
                        wrapped.close();
                    } finally {
                        closeQuietly(remoteFile);
                        connection.disconnectSftp(sftp);
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeIOException(format("Cannot write to %s", this), e);
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SshSftpFile.class);

}
