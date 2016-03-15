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
package com.xebialabs.overthere;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * An abstract representation of a file that can be access through an {@link OverthereConnection}. It could be a local
 * file or a remote file. It could be accessed from the local machine or through some remote connection methods (e.g.
 * SFTP, CIFS) or read from a repository. A file object can represent a regular file, a directory or some other
 * filesystem object such as links, symbolic links, pipes. It can also represent non-existent file.
 * <p/>
 * All methods in this interface may throw a {@link RuntimeIOException} if an error occurs. Checked {@link IOException
 * IOExceptions} are never thrown.
 */
public interface OverthereFile {

    /**
     * Return the connection through which this file is accessible. If the connection is closed, this file may no longer
     * be accessible.
     *
     * @return the connection through which this file is accessible.
     */
    OverthereConnection getConnection();

    /**
     * Return the full path of the file on the remote system.
     *
     * @return the full path of the file.
     */
    String getPath();

    /**
     * The name of the file on the remote system.
     *
     * @return the name of the file.
     */
    String getName();

    /**
     * Return a reference to the parent of this file or <code>null</code> if this file does not have a parent file. The
     * parent of a regular file is the directory that contains it, the parent of a directory is the directory that
     * contains that directory. A root directory does not have a parent.
     *
     * @return the parent of this file or <code>null</code> if this file does not have a parent file.
     */
    OverthereFile getParentFile();

    /**
     * Returns a reference to a named child of this file or <code>null</code> if this file is not a directory. The child
     * file returned may or may not exist.
     *
     * @param child the name of the child relative to this file. May not contain path separators.
     * @return the child of this file or <code>null</code> if this file is not a directory.
     */
    OverthereFile getFile(String child);

    /**
     * Tests whether the file represented by this object exists.
     *
     * @return <code>true</code> if and only if this file exists.
     * @throws RuntimeIOException if an I/O error occured
     */
    boolean exists();

    /**
     * Tests whether this file can be read.
     *
     * @return <code>true</code> if and only if this file can be read.
     */
    boolean canRead();

    /**
     * Tests whether this file can be written.
     *
     * @return <code>true</code> if and only if this file can be written.
     */
    boolean canWrite();

    /**
     * Tests whether this file can be executed.
     *
     * @return <code>true</code> if and only if this file can be executed.
     */
    boolean canExecute();

    /**
     * Tests whether this file is a regular file.
     *
     * @return <code>true</code> if and only if this file is a regular file.
     */
    boolean isFile();

    /**
     * Tests whether this file is a directory.
     *
     * @return <code>true</code> if and only if this file is a directory.
     */
    boolean isDirectory();

    /**
     * Tests whether this file is a hidden file.
     *
     * @return <code>true</code> if and only if this file is a hidden file.
     */
    boolean isHidden();

    /**
     * Returns the time this file was last modified, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     *
     * @return the time this file was last modified, in milliseconds since the epoch (00:00:00 GMT, January 1, 1970).
     */
    long lastModified();

    /**
     * Returns the length of this file, in bytes.
     *
     * @return the length of this file.
     */
    long length();

    /**
     * Returns an input stream to read from this file. The complete contents of this input stream must be read before
     * another operation on this file or its corresponding {@link OverthereConnection} is performed.
     *
     * @return an input stream connected to this file.
     */
    InputStream getInputStream();

    /**
     * Returns an output stream to write to this file. The complete contents of this output stream must be written
     * before another operation on this file or its corresponding {@link OverthereConnection} is performed.
     *
     * @return an output stream connected to this file.
     */
    OutputStream getOutputStream();

    /**
     * Sets the execute permission on this file.
     *
     * @param executable If <code>true</code>, sets the access permission to allow execute operations; if <code>false</code> to
     *                   disallow execute operations.
     */
    void setExecutable(boolean executable);

    /**
     * Deletes this file or directory. If this file is a directory and it is not empty, a {@link RuntimeIOException} is
     * thrown.
     */
    void delete();

    /**
     * Deletes this file or directory. If this file is a directory, its contents are deleted first.
     */
    void deleteRecursively();

    /**
     * Lists the files in this directory. If this file is not a directory, the outcome is unspecified.
     *
     * @return the files in this directory, in an unspecified order.
     */
    List<OverthereFile> listFiles();

    /**
     * Creates this directory. If the parent directory does not exists, a {@link RuntimeIOException} is thrown.
     */
    void mkdir();

    /**
     * Creates this directory and any of its parent directories.
     */
    void mkdirs();

    /**
     * Renames or moves this file. Usually, a file cannot be moved to a different type of connection and sometimes not
     * even to a different connection. Also, it depends on the {@link OverthereConnection connaction} implementation
     * whether or not the file can be moved accross file system boundaries within that connection.
     *
     * @param dest the new location for this file.
     */
    void renameTo(OverthereFile dest);

    /**
     * Copies this file or directory (recursively) to the destination. Contrary to the semantics of
     * {@link #renameTo(OverthereFile)}, a file <em>can</em> be copied accross {@link OverthereConnection connections}
     * and across file systems withtin a {@link OverthereConnection connection}.
     *
     * @param dest the file to copy to.
     */
    void copyTo(final OverthereFile dest);

}
