/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.spi;

import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Closeables.closeQuietly;
import static com.xebialabs.overthere.ConnectionOptions.CONNECTION_TIMEOUT_MILLIS;
import static com.xebialabs.overthere.ConnectionOptions.DEFAULT_CONNECTION_TIMEOUT_MILLIS;
import static com.xebialabs.overthere.ConnectionOptions.DEFAULT_TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT;
import static com.xebialabs.overthere.ConnectionOptions.DEFAULT_TEMPORARY_FILE_CREATION_RETRIES;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_FILE_CREATION_RETRIES;
import static com.xebialabs.overthere.util.OverthereUtils.getBaseName;
import static com.xebialabs.overthere.util.OverthereUtils.getExtension;

/**
 * A connection on a host (local or remote) on which to manipulate files and execute commands.
 * 
 * All methods in this interface may throw a {@link com.xebialabs.overthere.RuntimeIOException} if an error occurs.
 * Checked {@link java.io.IOException IOExceptions} are never thrown.
 */
public abstract class BaseOverthereConnection implements OverthereConnection {

    protected final String protocol;

    protected final OperatingSystemFamily os;

    protected final int connectionTimeoutMillis;

    protected final String temporaryDirectoryPath;

    protected final boolean deleteTemporaryDirectoryOnDisconnect;

    protected final int temporaryFileCreationRetries;

    protected final boolean canStartProcess;

    protected OverthereFile connectionTemporaryDirectory;

    protected OverthereFile workingDirectory;

    protected final AddressPortMapper mapper;

    protected BaseOverthereConnection(final String protocol, final ConnectionOptions options, final AddressPortMapper mapper, final boolean canStartProcess) {
        this.protocol = checkNotNull(protocol, "Cannot create OverthereConnection with null protocol");
        this.os = options.getEnum(OPERATING_SYSTEM, OperatingSystemFamily.class);
        this.connectionTimeoutMillis = options.getInteger(CONNECTION_TIMEOUT_MILLIS, DEFAULT_CONNECTION_TIMEOUT_MILLIS);
        this.temporaryDirectoryPath = options.get(TEMPORARY_DIRECTORY_PATH, os.getDefaultTemporaryDirectoryPath());
        this.deleteTemporaryDirectoryOnDisconnect = options.getBoolean(TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT,
            DEFAULT_TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT);
        this.temporaryFileCreationRetries = options.getInteger(TEMPORARY_FILE_CREATION_RETRIES, DEFAULT_TEMPORARY_FILE_CREATION_RETRIES);
        this.canStartProcess = canStartProcess;
        this.mapper = checkNotNull(mapper, "Cannot create OverthereConnection with null addres-port mapper");
    }

    /**
     * Return the OS family of the host.
     * 
     * @return the OS family
     */
    @Override
    public final OperatingSystemFamily getHostOperatingSystem() {
        return os;
    }

    /**
     * Closes the connection. Depending on the {@link ConnectionOptions#TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT}
     * connection option, deletes all temporary files that have been created on the host.
     */
    @Override
    public final void close() {
        if (deleteTemporaryDirectoryOnDisconnect) {
            deleteConnectionTemporaryDirectory();
        }

        doClose();

        closeQuietly(mapper);

        logger.info("Disconnected from {}", this);
    }

    /**
     * To be overridden by a base class to implement connection specific disconnection logic.
     */
    protected abstract void doClose();

    /**
     * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically
     * removed when this connection is closed. <b>N.B.:</b> The file is not actually created until a put method is
     * invoked.
     * 
     * @param nameTemplate
     *            the template on which to base the name of the temporary file. May be <code>null</code>.
     * @return a reference to the temporary file on the host
     */
    @Override
    public final OverthereFile getTempFile(String nameTemplate) {
        String prefix, suffix;

        if (nameTemplate != null) {
            int pos = nameTemplate.lastIndexOf('/');
            if (pos != -1) {
                nameTemplate = nameTemplate.substring(pos + 1);
            }
            pos = nameTemplate.lastIndexOf('\\');
            if (pos != -1) {
                nameTemplate = nameTemplate.substring(pos + 1);
            }
        }

        if (isNullOrEmpty(nameTemplate)) {
            prefix = "hostsession";
            suffix = ".tmp";
        } else {
            prefix = getBaseName(nameTemplate);
            suffix = getExtension(nameTemplate);
        }

        return getTempFile(prefix, suffix);
    }

    /**
     * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically
     * removed when this connection is closed. <b>N.B.:</b> The file is not actually created until a put method is
     * invoked.
     * 
     * @param prefix
     *            the prefix string to be used in generating the file's name; must be at least three characters long
     * @param suffix
     *            the suffix string to be used in generating the file's name; may be <code>null</code>, in which case
     *            the suffix ".tmp" will be used
     * @return a reference to the temporary file on the host
     */
    @Override
    public final OverthereFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
        if (prefix == null)
            throw new NullPointerException("prefix is null");

        if (suffix == null) {
            suffix = ".tmp";
        }

        Random r = new Random();
        String infix = "";
        for (int i = 0; i < temporaryFileCreationRetries; i++) {
            OverthereFile f = getFileForTempFile(getConnectionTemporaryDirectory(), prefix + infix + suffix);
            if (!f.exists()) {
                logger.debug("Created temporary file {}", f);
                return f;
            }
            infix = "-" + Long.toString(Math.abs(r.nextLong()));
        }
        throw new RuntimeIOException("Cannot generate a unique temporary file name on " + this);
    }

    private OverthereFile getConnectionTemporaryDirectory() throws RuntimeIOException {
        if (connectionTemporaryDirectory == null) {
            connectionTemporaryDirectory = createConnectionTemporaryDirectory();
        }
        return connectionTemporaryDirectory;
    }

    private OverthereFile createConnectionTemporaryDirectory() {
        OverthereFile temporaryDirectory = getFile(temporaryDirectoryPath);
        Random r = new Random();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS");
        String prefix = "overthere-" + dateFormat.format(new Date());
        String infix = "";
        String suffix = ".tmp";
        for (int i = 0; i < temporaryFileCreationRetries; i++) {
            OverthereFile tempDir = getFileForTempFile(temporaryDirectory, prefix + infix + suffix);
            if (!tempDir.exists()) {
                tempDir.mkdir();
                logger.info("Created connection temporary directory {}", tempDir);
                return tempDir;
            }
            infix = "-" + Long.toString(Math.abs(r.nextLong()));
        }
        throw new RuntimeIOException("Cannot create connection temporary directory on " + this);
    }

    private void deleteConnectionTemporaryDirectory() {
        if (connectionTemporaryDirectory != null) {
            try {
                logger.info("Deleting connection temporary directory {}", connectionTemporaryDirectory);
                connectionTemporaryDirectory.deleteRecursively();
            } catch (RuntimeException exc) {
                logger.warn("Got exception while deleting connection temporary directory {}. Ignoring it.", connectionTemporaryDirectory, exc);
            }
        }
    }

    /**
     * Invoked by {@link #getTempFile(String)} and {@link #createConnectionTemporaryDirectory()} to create an
     * {@link OverthereFile} object for a file or directory in the system or connection temporary directory.
     * 
     * @param parent
     *            parent of the file to create
     * @param name
     *            name of the file to create.
     * @return the created file object
     */
    protected abstract OverthereFile getFileForTempFile(OverthereFile parent, String name);

    /**
     * Returns the working directory.
     * 
     * @return the working directory, may be <code>null</code>.
     */
    @Override
    public OverthereFile getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * Sets the working directory in which commands are executed. If set to <code>null</code>, the working directory
     * that is used depends on the connection implementation.
     * 
     * @param workingDirectory
     *            the working directory, may be <code>null</code>.
     */
    @Override
    public void setWorkingDirectory(OverthereFile workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Executes a command with its arguments.
     * 
     * @param handler
     *            the handler that will be invoked when the executed command generated output.
     * @param commandLine
     *            the command line to execute.
     * @return the exit value of the executed command. Usually 0 on successful execution.
     */
    @Override
    public int execute(final OverthereProcessOutputHandler handler, final CmdLine commandLine) {
        final OverthereProcess process = startProcess(commandLine);
        Thread stdoutReaderThread = null;
        Thread stderrReaderThread = null;
        final CountDownLatch latch = new CountDownLatch(2);
        try {
            stdoutReaderThread = new Thread("Stdout reader thread for command " + commandLine + " on " + this) {
                @Override
                public void run() {
                    StringBuilder lineBuffer = new StringBuilder();
                    InputStreamReader stdoutReader = new InputStreamReader(process.getStdout());
                    latch.countDown();
                    try {
                        int cInt = stdoutReader.read();
                        while (cInt > -1) {
                            char c = (char) cInt;
                            handler.handleOutput(c);
                            if (c != '\r' && c != '\n') {
                                lineBuffer.append(c);
                            }
                            if (c == '\n') {
                                handler.handleOutputLine(lineBuffer.toString());
                                lineBuffer.setLength(0);
                            }
                            cInt = stdoutReader.read();
                        }
                    } catch (Exception exc) {
                        logger.error("An exception occured while reading from stdout", exc);
                    } finally {
                        closeQuietly(stdoutReader);
                        if (lineBuffer.length() > 0) {
                            handler.handleOutputLine(lineBuffer.toString());
                        }
                    }
                }
            };
            stdoutReaderThread.start();

            stderrReaderThread = new Thread("Stderr reader thread for command " + commandLine + " on " + this) {
                @Override
                public void run() {
                    StringBuilder lineBuffer = new StringBuilder();
                    InputStreamReader stderrReader = new InputStreamReader(process.getStderr());
                    latch.countDown();
                    try {
                        int readInt = stderrReader.read();
                        while (readInt > -1) {
                            char c = (char) readInt;
                            if (c != '\r' && c != '\n') {
                                lineBuffer.append(c);
                            }
                            if (c == '\n') {
                                handler.handleErrorLine(lineBuffer.toString());
                                lineBuffer.setLength(0);
                            }
                            readInt = stderrReader.read();
                        }
                    } catch (Exception exc) {
                        logger.error("An exception occured while reading from stderr", exc);
                    } finally {
                        closeQuietly(stderrReader);
                        if (lineBuffer.length() > 0) {
                            handler.handleErrorLine(lineBuffer.toString());
                        }
                    }
                }
            };
            stderrReaderThread.start();

            try {
                latch.await();
                return process.waitFor();
            } catch (InterruptedException exc) {
                Thread.currentThread().interrupt();

                logger.info("Execution interrupted, destroying the process.");
                process.destroy();

                throw new RuntimeIOException("Execution interrupted", exc);
            }
        } finally {
            if (stdoutReaderThread != null) {
                try {
                    // interrupt the stdout reader thread in case it is stuck waiting for output that will never come
                    stdoutReaderThread.interrupt();
                    stdoutReaderThread.join();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            if (stderrReaderThread != null) {
                try {
                    // interrupt the stdout reader thread in case it is stuck waiting for output that will never come
                    stderrReaderThread.interrupt();
                    stderrReaderThread.join();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

    }

    /**
     * Starts a command with its argument and returns control to the caller.
     * 
     * @param commandLine
     *            the command line to execute.
     * @return an object representing the executing command or <tt>null</tt> if this is not supported by the host
     *         connection.
     */
    @Override
    public OverthereProcess startProcess(CmdLine commandLine) {
        throw new UnsupportedOperationException("Cannot start a process on " + this);
    }
    
    
    public OverthereProcess startProcess(CmdLine commandLine,
        boolean homeDirSafe) {
      throw new UnsupportedOperationException("Cannot start a process on " + this);
    }
    

    /**
     * Checks whether a process can be started on this connection.
     * 
     * @return <code>true</code> if a process can be started on this connection, <code>false</code> otherwise
     */
    @Override
    public final boolean canStartProcess() {
        return canStartProcess;
    }

    /**
     * Subclasses MUST implement toString properly.
     */
    @Override
    public abstract String toString();

    private static Logger logger = LoggerFactory.getLogger(BaseOverthereConnection.class);


}
