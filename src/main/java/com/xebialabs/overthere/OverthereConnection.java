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
package com.xebialabs.overthere;

import java.io.Closeable;
import java.io.IOException;

public interface OverthereConnection extends Closeable {
	/**
	 * Return the OS family of the host.
	 *
	 * @return the OS family
	 */
	OperatingSystemFamily getHostOperatingSystem();

	/**
	 * Creates a reference to a file on the host.
	 *
	 * @param hostPath
	 *            the path of the host
	 * @return a reference to the file
	 */
	OverthereFile getFile(String hostPath);

	/**
	 * Creates a reference to a file in a directory on the host.
	 *
	 * @param parent
	 *            the reference to the directory on the host
	 * @param child
	 *            the name of the file in the directory
	 * @return a reference to the file in the directory
	 */
	OverthereFile getFile(OverthereFile parent, String child);

	/**
	 * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically removed when this connection is closed.
	 * <b>N.B.:</b> The file is not actually created until a put method is invoked.
	 *
	 * @param nameTemplate
	 *            the template on which to base the name of the temporary file. May be <code>null</code>.
	 * @return a reference to the temporary file on the host
	 */
	OverthereFile getTempFile(String nameTemplate);

	/**
	 * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically removed when this connection is closed.
	 * <b>N.B.:</b> The file is not actually created until a put method is invoked.
	 *
	 * @param prefix
	 *            the prefix string to be used in generating the file's name; must be at least three characters long
	 * @param suffix
	 *            the suffix string to be used in generating the file's name; may be <code>null</code>, in which case the suffix ".tmp" will be used
	 * @return a reference to the temporary file on the host
	 */
	OverthereFile getTempFile(String prefix, String suffix) throws RuntimeIOException;

	/**
	 * Returns the working directory.
	 *
	 * @return the working directory, may be <code>null</code>.
	 */
	OverthereFile getWorkingDirectory();

	/**
	 * Sets the working directory in which commands are executed. If set to <code>null</code>, the working directory that is used depends on the connection
	 * implementation.
	 *
	 * @param workingDirectory
	 *            the working directory, may be <code>null</code>.
	 */
	void setWorkingDirectory(OverthereFile workingDirectory);

	/**
	 * Executes a command with its arguments.
	 *
	 * @param handler
	 *            the handler that will be invoked when the executed command generated output.
	 * @param commandLine
	 *            the command line to execute.
	 * @return the exit value of the executed command. Usually 0 on successful execution.
	 */
	int execute(OverthereProcessOutputHandler handler, CmdLine commandLine);

	/**
	 * Starts a command with its argument and returns control to the caller.
	 *
	 * @param commandLine
	 *            the command line to execute.
	 * @return an object representing the executing command or <tt>null</tt> if this is not supported by the host connection.
	 */
	OverthereProcess startProcess(CmdLine commandLine);

	/**
	 * Checks whether a process can be started on this connection.
	 *
	 * @return <code>true</code> if a process can be started on this connection, <code>false</code> otherwise
	 */
	boolean canStartProcess();

	/**
	 * Closes the connection. Does not throw {@link IOException} but can throw {@link RuntimeIOException}
	 */
	@Override
	void close();
	
	/**
	 * Implementations MUST implement toString properly.
	 */
	@Override
	String toString();
}
