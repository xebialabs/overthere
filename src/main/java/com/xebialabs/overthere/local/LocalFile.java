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
package com.xebialabs.overthere.local;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.BaseOverthereFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;

/**
 * A local file.
 */
@SuppressWarnings("serial")
public class LocalFile extends BaseOverthereFile<LocalConnection> implements Serializable {

    protected File file;

    public LocalFile(LocalConnection connection, File file) {
        super(connection);
        this.file = file;
    }

    @Override
    public final LocalConnection getConnection() {
        if (connection == null) {
            connection = createConnection();
        }

        return connection;
    }

    private static LocalConnection createConnection() {
        // Creating LocalConnection directly instead of through Overthere.getConnection() to prevent log messages from
        // appearing
        LocalConnection localConnectionThatWillNeverBeDisconnected = new LocalConnection(LOCAL_PROTOCOL, new ConnectionOptions());
        // FIXME: Creating a LocalConnection on the fly does not honour the original TEMPORARY_DIRECTORY_PATH (tmp)
        // setting
        return localConnectionThatWillNeverBeDisconnected;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String getPath() {
        return file.getPath();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public OverthereFile getParentFile() {
        return getConnection().getFile(file.getParent());
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public long length() {
        return file.length();
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public boolean isFile() {
        return file.isFile();
    }

    @Override
    public boolean isDirectory() {
        return file.isDirectory();
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    @Override
    public boolean canRead() {
        return file.canRead();
    }

    @Override
    public boolean canWrite() {
        return file.canWrite();
    }

    @Override
    public boolean canExecute() {
        return file.canExecute();
    }

    @Override
    public void setExecutable(boolean executable) {
        logger.debug("Setting execute permission on {} to {}", this, executable);

        file.setExecutable(executable);
    }

    @Override
    public void delete() {
        logger.debug("Deleting {}", this);

        try {
            Files.delete(file.toPath());
        } catch (IOException io) {
            throw new RuntimeIOException("Cannot delete " + this, io);
        }
    }

    @Override
    public void mkdir() {
        logger.debug("Creating directory {}", this);

        if (!file.mkdir()) {
            throw new RuntimeIOException("Cannot mkdir " + this);
        }
    }

    @Override
    public void mkdirs() {
        logger.debug("Creating directory {}", this);

        if (!file.mkdirs()) {
            throw new RuntimeIOException("Cannot mkdir " + this);
        }
    }

    @Override
    public List<OverthereFile> listFiles() {
        logger.debug("Listing directory {}", this);

        List<OverthereFile> list = new ArrayList<OverthereFile>();
        for (File each : file.listFiles()) {
            list.add(new LocalFile(connection, each));
        }
        return list;
    }

    @Override
    protected void shortCircuitCopyFrom(OverthereFile source) {
        copyFrom(source);
    }

    @Override
    public void renameTo(OverthereFile dest) {
        logger.debug("Renaming {} to {}", this, dest);

        if (!(dest instanceof LocalFile)) {
            throw new RuntimeIOException("Destination is not a " + LocalFile.class.getName());
        }

        if (!file.renameTo(((LocalFile) dest).file)) {
            throw new RuntimeIOException("Cannot rename " + this + " to " + dest);
        }
    }

    @Override
    public InputStream getInputStream() {
        logger.debug("Opening file input stream for {}", this);

        try {
            return asBuffered(new FileInputStream(file) {
                @Override
                public void close() throws IOException {
                    super.close();
                    logger.debug("Closed file input stream for {}", LocalFile.this);
                }
            });
        } catch (FileNotFoundException exc) {
            throw new RuntimeIOException("Cannot open " + this + " for reading", exc);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        logger.debug("Opening file output stream for {}", this);

        try {
            return asBuffered(new FileOutputStream(file){
                @Override
                public void close() throws IOException {
                    super.close();
                    logger.debug("Closed file output stream for {}", LocalFile.this);
                }
            });
        } catch (FileNotFoundException exc) {
            throw new RuntimeIOException("Cannot open " + this + " for writing", exc);
        }
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof LocalFile))
            return false;

        return this.file.equals(((LocalFile) that).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public String toString() {
        return LOCAL_PROTOCOL + ":" + file;
    }

    public static OverthereFile valueOf(File f) {
        return new LocalFile(createConnection(), f);
    }

    public static LocalFile from(File f) {
        return new LocalFile(createConnection(), f);
    }

    private static Logger logger = LoggerFactory.getLogger(LocalFile.class);

}
