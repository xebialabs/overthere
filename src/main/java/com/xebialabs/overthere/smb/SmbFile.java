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
package com.xebialabs.overthere.smb;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.mserref.NtStatus;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileIdBothDirectoryInformation;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.protocol.commons.EnumWithValue;
import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.share.DiskEntry;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.File;
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
import java.util.Set;

import static java.lang.String.format;


public class SmbFile extends BaseOverthereFile<SmbConnection> {

    private final String hostPath;
    private boolean overwrite = true;
    private Map<String, String> pathMappings;

    public SmbFile(SmbConnection connection, String hostPath, Map<String, String> pathMappings) {
        super(connection);
        this.hostPath = SmbPaths.escapeForwardSlashes(hostPath);
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
        String parentPath = SmbPaths.getParentPath(hostPath);
        if (parentPath != null)
            f = getFileForAbsolutePath(parentPath);
        return f;
    }

    @Override
    public boolean exists() {
        return isFile() || isDirectory();
    }

    @Override
    public boolean canRead() {
        logger.debug("Checking whether {} can be read", this.getPath());
        return checkAccess(EnumSet.of(AccessMask.GENERIC_READ), AccessMask.FILE_READ_DATA);
    }

    @Override
    public boolean canWrite() {
        logger.debug("Checking whether {} can be write", this.getPath());
        return checkAccess(EnumSet.of(AccessMask.GENERIC_READ, AccessMask.GENERIC_WRITE), AccessMask.FILE_WRITE_DATA);
    }

    @Override
    public boolean canExecute() {
        logger.debug("Checking whether {} can execute", this.getPath());
        return checkAccess(EnumSet.of(AccessMask.GENERIC_EXECUTE), AccessMask.FILE_EXECUTE);
    }

    private boolean checkAccess(Set<AccessMask> requestAccesSet, AccessMask searchAccessMask) {
        try {
            int accessFlags = getAccessMask(requestAccesSet);
            return EnumWithValue.EnumUtils.isSet(accessFlags, searchAccessMask);
        } catch(SMBApiException e) {
            logger.warn("Acces denied to {} : {}", getPath(), e.getMessage());
            if (e.getStatus() == NtStatus.STATUS_ACCESS_DENIED) {
                return false;
            }
            throw e;
        }
    }

    private int getAccessMask(Set<AccessMask> requestAccesSet) {
    	String pathOnShare = getPathOnShare();
    	try (DiskEntry entry = getShare().open(pathOnShare, requestAccesSet, null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)) {
            return entry.getFileInformation().getAccessInformation().getAccessFlags();
        }
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
        return getShare().getFileInformation(getPathOnShare()).getStandardInformation().getEndOfFile();
    }

    @Override
    public InputStream getInputStream() throws RuntimeIOException {
        logger.debug("Opening SMB input stream for {}", getSharePath());
        final File file = getShare().openFile(getPathOnShare(),
                EnumSet.of(AccessMask.GENERIC_READ), null, SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null);

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
                file.close();
            }
        });
    }

    @Override
    public OutputStream getOutputStream() {
        logger.debug("Opening SMB output stream for {}", getSharePath());
        SMB2CreateDisposition createDisposition = SMB2CreateDisposition.FILE_OVERWRITE_IF;
        if (!overwrite) createDisposition = SMB2CreateDisposition.FILE_CREATE;
        final File file = getShare().openFile(getPathOnShare(), EnumSet.of(AccessMask.GENERIC_WRITE),
                null, SMB2ShareAccess.ALL, createDisposition, null);

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
                file.close();
            }
        });
    }

    @Override
    public void setExecutable(boolean executable) {
        // the execute permission does not exist on Windows
    }

    @Override
    public void delete() {
        delete(false);
    }

    @Override
    public void deleteRecursively() {
        delete(true);
    }

    @Override
    public List<OverthereFile> listFiles() {
        String sharePath = getPathOnShare();
        logger.debug("Listing directory {}", sharePath);
        try {
            List<OverthereFile> files = new ArrayList<OverthereFile>();
            for (FileIdBothDirectoryInformation info : getShare().list(sharePath)) {
                if (!info.getFileName().equals(".") && !info.getFileName().equals("..")) {
                    files.add(getFile(info.getFileName()));
                }
            }
            return files;
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
        final String srcPathOnShare = getPathOnShare();
        logger.debug("Renaming {} to {}", srcPathOnShare, dest);
        if (!(dest instanceof SmbFile)) {
            throw new RuntimeIOException(
                    format("Cannot move/rename smb:%s: file/directory %s  to non-smb:%s: file/directory %s",
                            connection.cifsConnectionType.toString().toLowerCase(), getSharePath(),
                            connection.cifsConnectionType.toString().toLowerCase(), dest));
        }
        SmbFile destSmbFile = (SmbFile) dest;
        DiskShare destShare = destSmbFile.getShare();
        DiskShare srcShare = getShare();
        if (!srcShare.getSmbPath().toUncPath().equalsIgnoreCase(destShare.getSmbPath().toUncPath())) {
            throw new RuntimeIOException(
                    format("Cannot move smb:%s: file/directory %s on the other share smb:%s: file/directory %s",
                            connection.cifsConnectionType.toString().toLowerCase(), getSharePath(),
                            connection.cifsConnectionType.toString().toLowerCase(), dest));
        }
        try ( DiskEntry srcEntry = isFile()
                ? srcShare.openFile(srcPathOnShare, EnumSet.of(AccessMask.DELETE), null,
                                SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)
                : srcShare.openDirectory(srcPathOnShare, EnumSet.of(AccessMask.DELETE), null,
                                SMB2ShareAccess.ALL, SMB2CreateDisposition.FILE_OPEN, null)
        ) {
            srcEntry.rename(destSmbFile.getPathOnShare());
        } catch (SMBApiException exc) {
            throw new RuntimeIOException(
                    format("Cannot move/rename %s to %s: %s", srcPathOnShare, dest, exc.toString()), exc);
        }
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

    private void delete(boolean recursive) {
        String sharePath = getPathOnShare();
        try {
            if (isFile()) {
                logger.debug("deleting file {}", sharePath);
                getShare().rm(sharePath);
            } else {
                logger.debug("deleting directory {}", sharePath);
                getShare().rmdir(sharePath, recursive);
            }
        } catch (SMBApiException e) {
            throw new RuntimeIOException(format("Cannot delete %s: %s", sharePath, e.toString()), e);
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
        long attrMask = getShare().getFileInformation(getPathOnShare()).getBasicInformation().getFileAttributes();
        return FileAttributes.EnumUtils.isSet(attrMask, mask);
    }

    private SmbFile getFileForAbsolutePath(String path) {
        return new SmbFile(getConnection(), path, pathMappings);
    }

    private static Logger logger = LoggerFactory.getLogger(SmbFile.class);
}
