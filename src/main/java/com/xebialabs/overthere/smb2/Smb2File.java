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
package com.xebialabs.overthere.smb2;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.msfscc.fileinformation.FileInfo;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
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

import static java.lang.String.format;

public class Smb2File extends BaseOverthereFile<Smb2Connection> {

    private final String hostPath;
    private boolean overWrite;

    public Smb2File(Smb2Connection connection, String hostPath) {
        super(connection);
        this.hostPath = hostPath;
    }

    @Override
    public String getPath() {
        return hostPath;
    }

    @Override
    public String getName() {
        int i = hostPath.lastIndexOf('\\');
        return hostPath.substring(i);
    }

    @Override
    public OverthereFile getParentFile() {
        OverthereFile f = null;
        String[] s = hostPath.split("\\\\");
        if (s.length > 1) {
            f = getFile(s[hostPath.length() - 1]);
        }
        return f;
    }

    @Override
    public boolean exists() {
        return isFile() || isDirectory();
    }

    @Override
    public boolean canRead() {
        return checkAccessMask(AccessMask.GENERIC_READ);
    }

    private boolean checkAccessMask(AccessMask mask) {
        long accessMask = connection.getShare().getFileInformation(hostPath).getAccessMask();
        return AccessMask.EnumUtils.isSet(accessMask, mask);
    }

    private boolean checkAttributes(FileAttributes mask) {
        long attrMask = connection.getShare().getFileInformation(hostPath).getFileAttributes();
        return FileAttributes.EnumUtils.isSet(attrMask, mask);
    }

    @Override
    public boolean canWrite() {
        return checkAccessMask(AccessMask.GENERIC_WRITE);
    }

    @Override
    public boolean canExecute() {
        return checkAccessMask(AccessMask.GENERIC_EXECUTE);
    }

    @Override
    public boolean isFile() {
        return connection.getShare().fileExists(hostPath);
    }

    @Override
    public boolean isDirectory() {
        return connection.getShare().folderExists(hostPath);
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
        return connection.getShare().getFileInformation(hostPath).getFileSize();
    }

    @Override
    public InputStream getInputStream() throws RuntimeIOException {
        logger.debug("Opening SMB2 input stream for {}", hostPath);
        try {
            final File file = connection.getShare().openFile(hostPath,
                    EnumSet.of(AccessMask.GENERIC_READ), SMB2CreateDisposition.FILE_OPEN);
            try {
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
                    public int available() throws IOException {
                        return wrapped.available();
                    }

                    @Override
                    public void close() throws IOException {
                        logger.debug("Closing SMB2 input stream for {}", hostPath);
                        wrapped.close();
                    }
                });
            } finally {
                file.close();
            }
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot open %s for reading: %s", hostPath, e.toString()), e);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        logger.debug("Opening SMB2 output stream for {}", hostPath);
        try {
            SMB2CreateDisposition createDisposition = SMB2CreateDisposition.FILE_OVERWRITE_IF;
            if (!overWrite) createDisposition = SMB2CreateDisposition.FILE_CREATE;
            final File file = connection.getShare().openFile(hostPath,
                    EnumSet.of(AccessMask.GENERIC_READ), createDisposition);

            try {
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
                        logger.debug("Closing SMB2 output stream for {}", hostPath);
                        wrapped.close();
                    }
                });
            } finally {
                file.close();
            }
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot open %s for writing: %s", hostPath, e.toString()), e);
        }
    }

    @Override
    public void setExecutable(boolean executable) {
        // the execute permission does not exist on Windows
    }

    @Override
    public void delete() {
        try {
            DiskShare share = connection.getShare();
            if (isFile()) {
                logger.debug("deleting file {}", hostPath);
                share.rm(hostPath);
            } else {
                logger.debug("deleting directory {}", hostPath);
                share.rmdir(hostPath, true);
            }
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot delete %s: %s", hostPath, e.toString()), e);
        }
    }

    @Override
    public void deleteRecursively() {
        logger.debug("deleting directory recursively {}", hostPath);
        try {
            connection.getShare().rmdir(hostPath, true);
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot delete recursively %s: %s", hostPath, e.toString()), e);
        }
    }

    @Override
    public List<OverthereFile> listFiles() {
        logger.debug("Listing directory {}", hostPath);
        try {
            List<OverthereFile> files = new ArrayList<OverthereFile>();
            for (FileInfo info : connection.getShare().list(hostPath)) {
                files.add(getFile(info.getFileName()));
            }
            return files;
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot list directory %s: %s", hostPath, e.toString()), e);
        }
    }

    @Override
    public void mkdir() {
        logger.debug("Creating directory {}", hostPath);
        try {
            connection.getShare().mkdir(hostPath);
        } catch (TransportException e) {
            throw new RuntimeIOException(format("Cannot create directory %s: %s", hostPath, e.toString()), e);
        }
    }

    @Override
    public void mkdirs() {
        logger.debug("Creating directories {}", hostPath);
        mkdir();
    }

    @Override
    public void renameTo(OverthereFile dest) {
        throw new RuntimeException("Operation not supported");
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof Smb2File)) {
            return false;
        }

        return getPath().equals(((Smb2File) that).getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public String toString() {
        return getConnection() + "/" + getPath();
    }

    private static Logger logger = LoggerFactory.getLogger(Smb2File.class);
}
