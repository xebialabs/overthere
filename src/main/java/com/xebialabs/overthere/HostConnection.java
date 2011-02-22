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

import java.io.File;
import java.util.Map;

/**
 * A connection on a host (local or remote) on which to manipulate files and execute commands.
 */
public interface HostConnection {

	/**
	 * Return the OS family of the host.
	 * 
	 * @return the OS family
	 */
	OperatingSystemFamily getHostOperatingSystem();

	/**
	 * Closes the host connection. Destroys any temporary files that may have been created on the host.
	 * 
	 * Never throws an exception, not even a {@link RuntimeException}
	 */
	void disconnect();

	/**
	 * Creates a reference to a file on the host.
	 * 
	 * @param hostPath
	 *            the path of the host
	 * @return a reference to the file
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	HostFile getFile(String hostPath) throws RuntimeIOException;

	/**
	 * Creates a reference to a file in a directory on the host.
	 * 
	 * @param parent
	 *            the reference to the directory on the host
	 * @param child
	 *            the name of the file in the directory
	 * @return a reference to the file in the directory
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	HostFile getFile(HostFile parent, String child) throws RuntimeIOException;

	/**
	 * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically
	 * removed when this connection is closed. <b>N.B.:</b> The file is not actually created until a put method is invoked.
	 * 
	 * @param nameTemplate
	 *            the template on which to base the name of the temporary file. May be <code>null</code>.
	 * @return a reference to the temporary file on the host
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	HostFile getTempFile(String nameTemplate) throws RuntimeIOException;

	/**
	 * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically
	 * removed when this connection is closed. <b>N.B.:</b> The file is not actually created until a put method is invoked.
	 * 
	 * @param prefix
	 *            the prefix string to be used in generating the file's name; must be at least three characters long
	 * @param suffix
	 *            the suffix string to be used in generating the file's name; may be <code>null</code>, in which case
	 *            the suffix ".tmp" will be used
	 * @return a reference to the temporary file on the host
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	HostFile getTempFile(String prefix, String suffix) throws RuntimeIOException;

	/**
	 * Executes a command with its arguments.
	 * 
	 * @param handler
	 *            the callback handler that will be invoked when the executed command generated output.
	 * @param cmdarray
	 *            the command line to execute. The first element is the command, the other elements are its arguments.
	 * @return the exit value of the executed command. Is 0 on succesfull execution.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	int execute(CommandExecutionCallbackHandler handler, String... cmdarray) throws RuntimeIOException;

	/**
	 * Executes a command and responds to any output with the provided responses.
	 * 
	 * @param handler
	 *            the callback handler that will be invoked when the executed command generated output.
	 * @param inputResponse
	 *            expected output and the response to send
	 * @param cmdarray
	 *            the command line to execute. The first element is the command, the other elements are its arguments.
	 * @return the exit value of the executed command. Is 0 on succesfull execution.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	int execute(CommandExecutionCallbackHandler handler, Map<String, String> inputResponse, String... cmdarray);

	/**
	 * Starts the execution of a command and gives the caller full control over the execution.
	 * 
	 * @param cmdarray
	 *            the command line to execute. The first element is the command, the other elements are its arguments.
	 * @return an object representing the executing command or <tt>null</tt> if this is not supported by the host
	 *         connection.
	 * @throws RuntimeIOException
	 *             if an I/O error occurs
	 */
	CommandExecution startExecute(String... cmdarray);

	/**
	 * Copies a local file to a temporary file on the host.
	 * 
	 * @param file
	 *            the local file to copy
	 * 
	 * @return the path of the temporary file on the host.
	 */
	HostFile copyToTemporaryFile(File file) throws RuntimeIOException;

}

