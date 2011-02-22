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
/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.overthere;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * A reference to a file on a host. This object is always associated with a {@link HostConnection}.
 */
public interface HostFile {

	/**
	 * Returns the connection this file is associated with.
	 * 
	 * @return the connection
	 */
	HostConnection getConnection();

	/**
	 * Returns a new HostFile with this file as its parent. Identical to invoking
	 * <code>this.getSession().getHostFile(this, name)</code>
	 * 
	 * @param name
	 *            the name of the file in the directory
	 * @return a reference to the file in the directory
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	HostFile getFile(String name) throws RuntimeIOException;

	/**
	 * Returns the path.
	 * 
	 * @return the path
	 */
	String getPath();

	/**
	 * Returns the name of the host file or directory.
	 */
	String getName();

	/**
	 * Returns the path of the parent of the host file or directory. The parent is the directory containing this file or
	 * directory. Returns the empty string if the host file or directory has no parent.
	 */
	String getParent();

	/**
	 * Returns the a new HostFile referring to the parent of the host file or directory. The parent is the directory
	 * containing this file or directory. Returns <code>null</code> if the host file or directory has no parent.
	 */
	HostFile getParentFile();

	/**
	 * Tests whether the file or directory exists.
	 * 
	 * @return <code>true</code> if and only if the file or directory exists on the host; <code>false</code> otherwise
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	boolean exists() throws RuntimeIOException;

	/**
	 * Tests whether this file is a directory.
	 * 
	 * @return <code>true</code> if and only if the file exists <em>and</em> is a directory; <code>false</code>
	 *         otherwise
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	boolean isDirectory() throws RuntimeIOException;

	/**
	 * Returns the length of this file.
	 * 
	 * @return the size of this file in bytes.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	long length() throws RuntimeIOException;

	/**
	 * Tests whether the file or directory can be read.
	 * 
	 * @return <code>true</code> if and only if the file or directory is readable on the host; <code>false</code>
	 *         otherwise
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	boolean canRead() throws RuntimeIOException;

	/**
	 * Tests whether the file or directory can be written.
	 * 
	 * @return <code>true</code> if and only if the file or directory is writable on the host; <code>false</code>
	 *         otherwise
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	boolean canWrite() throws RuntimeIOException;

	/**
	 * Tests whether the file or directory can be executed.
	 * 
	 * @return <code>true</code> if and only if the file or directory is executable on the host; <code>false</code>
	 *         otherwise
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	boolean canExecute() throws RuntimeIOException;

	/**
	 * Returns a list of strings naming the files and directories contained by the host directory. Names denoting the
	 * directory itself and the directory's parent directory are not included in the result. Each string is a file name
	 * rather than a complete path.
	 * 
	 * @return An list of strings naming the files and directories in the host directory. The list will be empty if the
	 *         directory is empty.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs or this file does not denote a directory.
	 */
	List<String> list() throws RuntimeIOException;

	/**
	 * Returns a list of HostFile objects for the files and directories contained by the host directory. HostFile
	 * objects denoting the directory itself and the directory's parent directory are not included in the result.
	 * 
	 * @return An list of HostFile objects for the files and directories in the host directory. The list will be empty
	 *         if the directory is empty.
	 * 
	 * @throws RuntimeIOException
	 *             if an I/O error occurs or this file does not denote a directory.
	 */
	List<HostFile> listFiles() throws RuntimeIOException;

	/**
	 * Creates the directory on the host.
	 * 
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	void mkdir() throws RuntimeIOException;

	/**
	 * Creates the directory on the host, including any missing parent directories if needed.
	 * 
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	void mkdirs() throws RuntimeIOException;

	/**
	 * Moves/renames this file to the target file atomically.
	 * 
	 * @param destFile
	 *            the destination file.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs or the underlying implementation cannot move/rename the file.
	 */
	void moveTo(HostFile destFile);

	/**
	 * Deletes the host file or directory. If the file or directory does not exists on the host, <code>false</code> is
	 * returned.
	 * 
	 * @return <code>true</code> if the host file or directory existed before the deletion; <code>false</code> otherwise
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	boolean delete() throws RuntimeIOException;

	/**
	 * Deletes the host directory recursively, first deleting its contents and then the directory itself. If the host
	 * directory is actually a file, invoking this method is identical to invoking the {@link #delete()} method. If the
	 * directory does not exists on the host, <code>false</code> is returned.
	 * 
	 * @return <code>true</code> if the host file or directory existed before the deletion; <code>false</code> otherwise
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	boolean deleteRecursively() throws RuntimeIOException;


	/**
	 * Opens the host file for reading.
	 * 
	 * @return the InputStream connected to the file.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	InputStream get() throws RuntimeIOException;

	/**
	 * Copies the content of the host file to a stream.
	 * 
	 * @param out
	 *            the stream to copy to
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	void get(OutputStream out) throws RuntimeIOException;

	/**
	 * Copies the content of the host file to a file.
	 * 
	 * @param file
	 *            the file to copy to
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	void get(File file) throws RuntimeIOException;

	/**
	 * Opens the host file for writing.
	 * 
	 * @param length
	 *            the number of bytes that will be written to the stream
	 * @return the OutputStream connected to the file.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	OutputStream put(long length) throws RuntimeIOException;

	/**
	 * Copies the contents of a stream to the host file.
	 * 
	 * @param in
	 *            the stream to copy from
	 * @param length
	 *            the number of bytes that will be written to the stream
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	void put(InputStream in, long length) throws RuntimeIOException;

	/**
	 * Copies the contact of the local file to the host file.
	 * 
	 * @param file
	 *            the file to copy from
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	void put(File file) throws RuntimeIOException;

}

