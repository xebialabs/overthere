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
package com.xebialabs.overthere.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.spi.BaseOverthereFile;

import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.OperatingSystemFamily.getLocalHostOperatingSystemFamily;

/**
 * OverthereFile backed by a byte array.
 */
public class ByteArrayFile extends BaseOverthereFile<ByteArrayConnection> {

    private String path;

    private byte[] contents;

    public ByteArrayFile(String path, byte[] contents) {
        super(createConnection());
        this.path = path;
        this.contents = contents;
    }

    private static ByteArrayConnection createConnection() {
        ConnectionOptions options = new ConnectionOptions();
        OperatingSystemFamily os = getLocalHostOperatingSystemFamily();
        options.set(OPERATING_SYSTEM, os);
        options.set(TEMPORARY_DIRECTORY_PATH, os.getDefaultTemporaryDirectoryPath());
        return new ByteArrayConnection("byte_array", options);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getName() {
        return OverthereUtils.getName(path);
    }

    @Override
    public OverthereFile getParentFile() {
        return null;
    }

    @Override
    public OverthereFile getFile(String child) {
        return null;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public boolean canExecute() {
        return false;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return false;
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public long length() {
        return contents.length;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(contents);
    }

    @Override
    public OutputStream getOutputStream() {
        throw new UnsupportedOperationException("Cannot write to a byte array file");
    }

    @Override
    public void setExecutable(boolean executable) {
        throw new UnsupportedOperationException("Cannot set permission of a byte array file");
    }

    @Override
    public void delete() {
        throw new UnsupportedOperationException("Cannot delete a byte array file");
    }

    @Override
    public void deleteRecursively() {
        throw new UnsupportedOperationException("Cannot delete a byte array file");
    }

    @Override
    public List<OverthereFile> listFiles() {
        return new ArrayList<OverthereFile>();
    }

    @Override
    public void mkdir() {
        throw new UnsupportedOperationException("Cannot mkdir a byte array file");
    }

    @Override
    public void mkdirs() {
        throw new UnsupportedOperationException("Cannot mkdirs a byte array file");
    }

    @Override
    public void renameTo(OverthereFile dest) {
        throw new UnsupportedOperationException("Cannot rename a byte array file");
    }

    @Override
    public String toString() {
        return "byte_array://" + path;
    }

}
