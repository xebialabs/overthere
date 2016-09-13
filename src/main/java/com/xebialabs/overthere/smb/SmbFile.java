/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 * <p/>
 * <p/>
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 * <p/>
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.smb;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mserref.NtStatus;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileInfo;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.smbj.common.SMBApiException;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
import com.hierynomus.smbj.transport.TransportException;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.BaseOverthereFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.hierynomus.msdtyp.AccessMask.FILE_READ_DATA;
import static java.lang.String.format;

public class SmbFile extends BaseOverthereFile<SmbConnection> {

    private final String hostPath;
    private boolean overwrite = true;
    private Map<String, String> pathMappings;

    public SmbFile(SmbConnection connection, String hostPath, Map<String, String> pathMappings) {
        super(connection);
        this.hostPath = hostPath;
        this.pathMappings = pathMappings;
    }

    @Override
    public String getPath() {
        return hostPath;
    }

    @Override
    public String getName() {
        return SmbPaths.getFileName(getPathOnShare());
    }

    @Override
    public OverthereFile getFile(String child) {
        return new SmbFile(getConnection(), SmbPaths.join(hostPath, child), pathMappings);
    }

    @Override
    public OverthereFile getParentFile() {
        OverthereFile f = null;
        String parentPath = SmbPaths.getParentPath(getPathOnShare());
        if (parentPath != null)
            f = getFile(parentPath);
        return f;
    }

    @Override
    public boolean exists() {
        return isFile() || isDirectory();
    }

    @Override
    public boolean canRead() {
        return checkAccessMask(FILE_READ_DATA);
    }

    @Override
    public boolean canWrite() {
        return checkAccessMask(AccessMask.FILE_WRITE_DATA);
    }

    @Override
    public boolean canExecute() {
        return checkAccessMask(FILE_READ_DATA);
    }

    @Override
    public boolean isFile() {
        try {
            return getShare().fileExists(getPathOnShare());
        } catch (SMBApiException e) {
            if (e.getStatus().equals(NtStatus.STATUS_FILE_IS_A_DIRECTORY) ||
                    e.getStatus().equals(NtStatus.STATUS_OBJECT_PATH_NOT_FOUND))
                return false;
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public boolean isDirectory() {
        try {
            return getShare().folderExists(getPathOnShare());
        } catch (SMBApiException e) {
            if (e.getStatus().equals(NtStatus.STATUS_NOT_A_DIRECTORY) ||
                    e.getStatus().equals(NtStatus.STATUS_OBJECT_PATH_NOT_FOUND))
                return false;
            throw new RuntimeIOException(e);
        }
    }

    @Override
    public boolean isHidden() {
        return checkAttributes(FileAttributes.FILE_ATTRIBUTE_HIDDEN);
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public long length() {
        return getShare().getFileInformation(getPathOnShare()).getFileSize();
    }

    @Override
    public InputStream getInputStream() throws RuntimeIOException {
        logger.debug("Opening SMB input stream for {}", getSharePath());
        try {
            final File file = getShare().openFile(getPathOnShare(),
                    EnumSet.of(AccessMask.GENERIC_READ), SMB2CreateDisposition.FILE_OPEN);

            final InputStream wrapped = file.getInputStream();
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
                public void close() throws IOException {
                    logger.debug("Closing SMB input stream for {}", getSharePath());
                    wrapped.close();
                    getShare().close(file.getFileId());
                }
            });
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot open %s for reading: %s", getSharePath(), e.toString()), e);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        logger.debug("Opening SMB output stream for {}", getSharePath());
        try {
            SMB2CreateDisposition createDisposition = SMB2CreateDisposition.FILE_OVERWRITE_IF;
            if (!overwrite) createDisposition = SMB2CreateDisposition.FILE_CREATE;
            final File file = getShare().openFile(getPathOnShare(),
                    EnumSet.of(AccessMask.GENERIC_WRITE), createDisposition);

            final OutputStream wrapped = file.getOutputStream();

            return asBuffered(new OutputStream() {

                @Override
                public void write(int b) throws IOException {
                    wrapped.write(b);
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    wrapped.write(b, off, len);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    wrapped.write(b);
                }

                @Override
                public void flush() throws IOException {
                    wrapped.flush();
                }

                @Override
                public void close() throws IOException {
                    logger.debug("Closing SMB output stream for {}", getSharePath());
                    wrapped.close();
                    getShare().close(file.getFileId());
                }
            });
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot open %s for writing: %s", getSharePath(), e.toString()), e);
        }
    }

    @Override
    public void setExecutable(boolean executable) {
        // the execute permission does not exist on Windows
    }

    @Override
    public void delete() {
        String sharePath = getPathOnShare();
        try {
            if (isFile()) {
                logger.debug("deleting file {}", sharePath);
                getShare().rm(sharePath);
            } else {
                logger.debug("deleting directory {}", sharePath);
                getShare().rmdir(sharePath, false);
            }
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot delete %s: %s", sharePath, e.toString()), e);
        } catch (SMBApiException e) {
            throw new RuntimeIOException(format("Cannot delete %s: %s", sharePath, e.toString()), e);
        }
    }

    @Override
    public void deleteRecursively() {
        String sharePath = getPathOnShare();
        logger.debug("deleting directory recursively {}", sharePath);
        try {
            if (isFile())
                delete();
            else
                getShare().rmdir(sharePath, true);
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot delete recursively %s: %s", sharePath, e.toString()), e);
        }
    }

    @Override
    public List<OverthereFile> listFiles() {
        String sharePath = getPathOnShare();
        logger.debug("Listing directory {}", sharePath);
        try {
            List<OverthereFile> files = new ArrayList<OverthereFile>();
            for (FileInfo info : getShare().list(sharePath)) {
                files.add(getFile(info.getFileName()));
            }
            return files;
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot list directory %s: %s", sharePath, e.toString()), e);
        } catch (SMBApiException e) {
            throw new RuntimeIOException(format("Cannot list directory %s: %s", sharePath, e.toString()), e);
        }
    }

    @Override
    public void mkdir() {
        makeDirectory(getPathOnShare());
    }

    private void makeDirectory(String path) {
        String sharePath = getPathOnShare();
        logger.debug("Creating directory {}", sharePath);
        try {
            getShare().mkdir(path);
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot create directory %s: %s", sharePath, e.toString()), e);
        } catch (SMBApiException e) {
            throw new RuntimeIOException(format("Cannot create directory %s: %s", sharePath, e.toString()), e);
        }
    }

    @Override
    public void mkdirs() {
        String sharePath = getPathOnShare();
        logger.debug("Creating directories {}", sharePath);
        String[] paths = SmbPaths.getPathListFromOuterToInner(sharePath);
        for (String p : paths) {
            if (!getShare().folderExists(p))
                makeDirectory(p);
        }
    }

    @Override
    public void renameTo(OverthereFile dest) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof SmbFile)) {
            return false;
        }
        return getPath().equals(((SmbFile) that).getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public String toString() {
        return getConnection() + "/" + getPath();
    }

    private boolean checkAccessMask(AccessMask mask) {
        File file = null;
        try {
            file = getShare().openFile(getPathOnShare(), EnumSet.of(mask), SMB2CreateDisposition.FILE_OPEN);
            return file !=null;
        } catch (TransportException e) {
            throw new IllegalStateException("Exception occurred while trying to determine permissions on file", e);
        } catch (SMBApiException e) {
            return checkPermissions(e);
        } finally {
            close(file);
        }
    }

    private boolean checkPermissions(SMBApiException e) {
        if (e.getStatus().equals(NtStatus.STATUS_ACCESS_DENIED)) {
            return false;
        }
        throw new IllegalStateException("Exception occurred while trying to determine permissions on file", e);
    }

    private void close(File file) {
        try {
            getShare().close(file.getFileId());
        } catch (TransportException e) {
            throw new IllegalStateException("Exception occured while trying to determine permissions on file", e);
        }
    }

    private String getSharePath() {
        return SmbPaths.getSharePath(hostPath, pathMappings);
    }

    private String getPathOnShare() {
        return SmbPaths.getPathOnShare(getSharePath());
    }

    private DiskShare getShare() {
        String shareName = SmbPaths.getShareName(getSharePath());
        return connection.getShare(shareName);
    }

    private boolean checkAttributes(FileAttributes mask) {
        long attrMask = getShare().getFileInformation(getPathOnShare()).getFileAttributes();
        return FileAttributes.EnumUtils.isSet(attrMask, mask);
    }

    private static Logger logger = LoggerFactory.getLogger(SmbFile.class);
}
