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
package com.xebialabs.overthere.cifs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.BaseOverthereFile;

import static java.lang.String.format;

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
            String parent = smbFile.getParent();
            SmbFile parentSmbFile = new SmbFile(parent, connection.authentication);
            String share = parentSmbFile.getShare();
            if (null == share) {
                return null;
            }
            return new CifsFile(getConnection(), parentSmbFile);
        } catch (MalformedURLException exc) {
            return null;
        }
    }

    @Override
    public boolean exists() throws RuntimeIOException {
        logger.debug("Checking for existence of {}", smbFile.getUncPath());

        try {
            return smbFile.exists();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot determine existence of %s: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public boolean canRead() throws RuntimeIOException {
        logger.debug("Checking whether {} can be read", smbFile.getUncPath());

        try {
            return smbFile.canRead();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot determine whether %s can be read: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public boolean canWrite() throws RuntimeIOException {
        logger.debug("Checking whether {} can be written", smbFile.getUncPath());

        try {
            return smbFile.canWrite();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot determine whether %s can be written: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public boolean canExecute() throws RuntimeIOException {
        logger.debug("Checking whether {} can be executed", smbFile.getUncPath());

        try {
            return smbFile.canRead();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot determine whether %s can be executed: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public boolean isFile() throws RuntimeIOException {
        logger.debug("Checking whether {} is a file", smbFile.getUncPath());

        try {
            return smbFile.isFile();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot determine whether %s is a file: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public boolean isDirectory() throws RuntimeIOException {
        logger.debug("Checking whether {} is a directory", smbFile.getUncPath());

        try {
            return smbFile.isDirectory();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot determine whether %s is a directory: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public boolean isHidden() {
        logger.debug("Checking whether {} is hidden", smbFile.getUncPath());

        try {
            return smbFile.isHidden();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot determine whether %s is hidden: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public long lastModified() {
        logger.debug("Retrieving last modification date of {}", smbFile.getUncPath());

        try {
            return smbFile.lastModified();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot determine last modification timestamp of %s: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public long length() throws RuntimeIOException {
        logger.debug("Retrieving length of {}", smbFile.getUncPath());

        try {
            return smbFile.length();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot determine length of file %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public List<OverthereFile> listFiles() throws RuntimeIOException {
        logger.debug("Listing directory {}", smbFile.getUncPath());

        try {
            upgradeToDirectorySmbFile();
            List<OverthereFile> files = new ArrayList<OverthereFile>();
            for (String name : smbFile.list()) {
                files.add(getFile(name));
            }
            return files;
        } catch (MalformedURLException exc) {
            throw new RuntimeIOException(format("Cannot list directory %s: %s", smbFile.getUncPath(), exc.toString()), exc);
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot list directory %s: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public void mkdir() throws RuntimeIOException {
        logger.debug("Creating directory {}", smbFile.getUncPath());

        try {
            smbFile.mkdir();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot create directory %s: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public void mkdirs() throws RuntimeIOException {
        logger.debug("Creating directories {}", smbFile.getUncPath());

        try {
            smbFile.mkdirs();
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot create directories %s: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public void renameTo(OverthereFile dest) throws RuntimeIOException {
        logger.debug("Renaming {} to {}", smbFile.getUncPath(), dest);

        if (dest instanceof CifsFile) {
            SmbFile targetSmbFile = ((CifsFile) dest).getSmbFile();
            try {
                smbFile.renameTo(targetSmbFile);
            } catch (SmbException exc) {
                throw new RuntimeIOException(format("Cannot move/rename %s to %s: %s", smbFile.getUncPath(), dest, exc.toString()), exc);
            }
        } else {
            throw new RuntimeIOException(format("Cannot move/rename cifs:%s: file/directory %s  to non-cifs:%s: file/directory %s",
                    connection.cifsConnectionType.toString().toLowerCase(), smbFile.getUncPath(), connection.cifsConnectionType.toString().toLowerCase(), dest));
        }
    }

    @Override
    public void setExecutable(boolean executable) {
        // the execute permission does not exist on Windows
    }

    @Override
    public void delete() throws RuntimeIOException {
        logger.debug("Deleting {}", smbFile.getUncPath());

        try {
            if (smbFile.isDirectory()) {
                upgradeToDirectorySmbFile();
                if (smbFile.list().length > 0) {
                    throw new RuntimeIOException(format("Cannot delete non-empty directory %s", smbFile.getUncPath()));
                }
            }
            smbFile.delete();
            refreshSmbFile();
        } catch (MalformedURLException exc) {
            throw new RuntimeIOException(format("Cannot delete %s: %s", smbFile.getUncPath(), exc.toString()), exc);
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot delete %s: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public void deleteRecursively() throws RuntimeIOException {
        logger.debug("Deleting {} recursively", smbFile.getUncPath());

        try {
            if (smbFile.isDirectory()) {
                upgradeToDirectorySmbFile();
            }
            smbFile.delete();
            refreshSmbFile();
        } catch (MalformedURLException exc) {
            throw new RuntimeIOException(format("Cannot delete %s recursively: %s", smbFile.getUncPath(), exc.toString()), exc);
        } catch (SmbException exc) {
            throw new RuntimeIOException(format("Cannot delete %s recursively: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public InputStream getInputStream() throws RuntimeIOException {
        logger.debug("Opening CIFS input stream for {}", smbFile.getUncPath());

        try {
            final InputStream wrapped = smbFile.getInputStream();
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
                    logger.debug("Closing CIFS input stream for {}", CifsFile.this.smbFile.getUncPath());
                    wrapped.close();
                }
            });
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot open %s for reading: %s", smbFile.getUncPath(), exc.toString()), exc);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        logger.debug("Opening CIFS output stream for {}", smbFile.getUncPath());

        try {
            final OutputStream wrapped = smbFile.getOutputStream();

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
                    logger.debug("Closing CIFS output stream for {}", CifsFile.this.smbFile.getUncPath());
                    wrapped.close();
                }
            });
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot open %s for writing: %s", smbFile.getUncPath(), exc.toString()), exc);
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

    private static Logger logger = LoggerFactory.getLogger(CifsFile.class);

}
