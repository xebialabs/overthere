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

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents an executing process.
 */
public interface OverthereProcess {

    /**
     * Returns an output stream that is connected to the standard input stream (stdin) of the process.
     *
     * @return the input stream
     */
    OutputStream getStdin();

    /**
     * Returns an input stream that is connected to the standard output stream (stdout) of the process.
     *
     * @return the output stream
     */
    InputStream getStdout();

    /**
     * Returns an input stream that is connected to the standard error stream (stderr) of the process.
     *
     * @return the output stream
     */
    InputStream getStderr();

    /**
     * Waits for the command to complete its execution. Returns immediately if the process has already terminated.
     *
     * @return the exit value of the process
     * @throws InterruptedException if this method was interrupted
     */
    int waitFor() throws InterruptedException;

    /**
     * Forcibly terminates the process. Returns immediately if the process has already terminated.
     */
    void destroy();

    /**
     * Returns the exit value for the process.
     *
     * @return the exit value of the process
     * @throws IllegalThreadStateException if the process has not yet terminated.
     */
    int exitValue() throws IllegalThreadStateException;

}
