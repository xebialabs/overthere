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
package com.xebialabs.overthere.spi;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereExecutionOutputHandler;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;
import org.slf4j.MDC;

import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;
import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.util.ConsoleOverthereExecutionOutputHandler.syserrHandler;
import static com.xebialabs.overthere.util.ConsoleOverthereExecutionOutputHandler.sysoutHandler;
import static com.xebialabs.overthere.util.OverthereProcessOutputHandlerWrapper.wrapStderr;
import static com.xebialabs.overthere.util.OverthereProcessOutputHandlerWrapper.wrapStdout;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static java.lang.String.format;

/**
 * A connection on a host (local or remote) on which to manipulate files and execute commands.
 * <p/>
 * All methods in this interface may throw a {@link com.xebialabs.overthere.RuntimeIOException} if an error occurs.
 * Checked {@link java.io.IOException IOExceptions} are never thrown.
 */
public abstract class BaseOverthereConnection implements OverthereConnection {

    private static Logger logger = LoggerFactory.getLogger(BaseOverthereConnection.class);
    protected final String protocol;
    protected final ConnectionOptions options;
    protected final AddressPortMapper mapper;
    protected final OperatingSystemFamily os;
    protected final int connectionTimeoutMillis;
    protected final int socketTimeoutMillis;
    protected final boolean canStartProcess;
    protected final String temporaryDirectoryPath;
    protected final boolean deleteTemporaryDirectoryOnDisconnect;
    protected final int temporaryFileCreationRetries;
    protected final String temporaryFileHolderDirectoryNamePrefix;
    protected final List<OverthereFile> temporaryFileHolderDirectories = new ArrayList<OverthereFile>();
    protected final int streamBufferSize;
    protected int temporaryFileHolderDirectoryNameSuffix = 0;
    protected OverthereFile workingDirectory;
    private volatile boolean isConnected;
    private Throwable openStack;

    protected BaseOverthereConnection(final String protocol, final ConnectionOptions options, final AddressPortMapper mapper, final boolean canStartProcess) {
        this.protocol = checkNotNull(protocol, "Cannot create OverthereConnection with null protocol");
        this.options = checkNotNull(options, "Cannot create OverthereConnection with null options");
        this.mapper = checkNotNull(mapper, "Cannot create OverthereConnection with null address-port mapper");
        this.os = options.getEnum(OPERATING_SYSTEM, OperatingSystemFamily.class);
        this.connectionTimeoutMillis = options.getInteger(CONNECTION_TIMEOUT_MILLIS, CONNECTION_TIMEOUT_MILLIS_DEFAULT);
        this.socketTimeoutMillis = options.getInteger(SOCKET_TIMEOUT_MILLIS, SOCKET_TIMEOUT_MILLIS_DEFAULT);
        this.canStartProcess = canStartProcess;
        this.temporaryDirectoryPath = options.get(TEMPORARY_DIRECTORY_PATH, os.getDefaultTemporaryDirectoryPath());
        this.deleteTemporaryDirectoryOnDisconnect = options.getBoolean(TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT, TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT_DEFAULT);
        this.temporaryFileCreationRetries = options.getInteger(TEMPORARY_FILE_CREATION_RETRIES, TEMPORARY_FILE_CREATION_RETRIES_DEFAULT);
        this.temporaryFileHolderDirectoryNamePrefix = "ot-" + (new SimpleDateFormat("yyyyMMdd'T'HHmmssSSS")).format(new Date());
        this.streamBufferSize = options.getInteger(REMOTE_COPY_BUFFER_SIZE, REMOTE_COPY_BUFFER_SIZE_DEFAULT);
    }

    protected void connected() {
        this.isConnected = true;
        this.openStack = new Throwable("Opened here...");
    }

    /**
     * Return the OS family of the host.
     *
     * @return the OS family
     */
    @Override
    public OperatingSystemFamily getHostOperatingSystem() {
        return os;
    }

    /**
     * Closes the connection. Depending on the {@link ConnectionOptions#TEMPORARY_DIRECTORY_DELETE_ON_DISCONNECT}
     * connection option, deletes all temporary files that have been created on the host.
     */
    @Override
    public final void close() {
        if (!isConnected) {
            return;
        }

        try {
            if (deleteTemporaryDirectoryOnDisconnect) {
                deleteConnectionTemporaryDirectory();
            }

            doClose();

            closeQuietly(mapper);

        } finally {
            logDisconnect();
            isConnected = false;
        }
    }

    protected void logDisconnect() {
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
     * @param prefix the prefix string to be used in generating the file's name; must be at least three characters long
     * @param suffix the suffix string to be used in generating the file's name; may be <code>null</code>, in which case
     *               the suffix ".tmp" will be used
     * @return a reference to the temporary file on the host
     */
    @Override
    public final OverthereFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
        checkNotNull(prefix, "prefix is null");

        if (suffix == null) {
            suffix = ".tmp";
        }

        return getTempFile(prefix + suffix);
    }

    /**
     * Creates a reference to a temporary file on the host. This file has a unique name and will be automatically
     * removed when this connection is closed. <b>N.B.:</b> The file is not actually created until a put method is
     * invoked.
     *
     * @param name the name of the temporary file. May be <code>null</code>.
     * @return a reference to the temporary file on the host
     */
    @Override
    public final synchronized OverthereFile getTempFile(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "tmp";
        }

        OverthereFile temporaryDirectory = getFile(temporaryDirectoryPath);
        RuntimeException originalExc = null;
        for (int i = 0; i <= temporaryFileCreationRetries; i++) {
            String holderName = temporaryFileHolderDirectoryNamePrefix;
            if (temporaryFileHolderDirectoryNameSuffix > 0) {
                holderName += "." + temporaryFileHolderDirectoryNameSuffix;
            }
            OverthereFile holder = getFileForTempFile(temporaryDirectory, holderName);
            if (!holder.exists()) {
                logger.trace("Creating holder directory {} for temporary file with name {}", holder, name);
                try {
                    originalExc = null;
                    holder.mkdir();
                    temporaryFileHolderDirectories.add(holder);
                    OverthereFile tempFile = holder.getFile(name);
                    logger.debug("Generated temporary file name {}", tempFile);
                    return tempFile;
                } catch(RuntimeException exc) {
                    originalExc = exc;
                    logger.debug(format("Failed to create holder directory %s - Trying with the next suffix", holder), exc);
                }
            }
            temporaryFileHolderDirectoryNameSuffix++;
        }

        if(originalExc != null) {
            throw new RuntimeIOException("Cannot generate a unique temporary file name on " + this, originalExc);
        } else {
            throw new RuntimeIOException("Cannot generate a unique temporary file name on " + this);
        }
    }

    private void deleteConnectionTemporaryDirectory() {
        for (OverthereFile d : temporaryFileHolderDirectories) {
            try {
                logger.info("Deleting temporary directory {}", d);
                d.deleteRecursively();
            } catch (RuntimeException exc) {
                logger.warn("Got exception while deleting connection temporary directory {}. Ignoring it.", d, exc);
            }
        }
    }

    /**
     * Invoked by {@link #getTempFile(String)} and {@link #getTempFile(String)} to create an
     * {@link OverthereFile} object for a file or directory in the system or connection temporary directory.
     *
     * @param parent parent of the file to create
     * @param name   name of the file to create.
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
     * @param workingDirectory the working directory, may be <code>null</code>.
     */
    @Override
    public void setWorkingDirectory(OverthereFile workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Returns the connection options used to construct this connection.
     *
     * @return the connection options.
     */
    public ConnectionOptions getOptions() {
        return options;
    }

    @Override
    public final int execute(final CmdLine commandLine) {
        return execute(sysoutHandler(), syserrHandler(), commandLine);
    }

    @Override
    public int execute(final OverthereExecutionOutputHandler stdoutHandler, final OverthereExecutionOutputHandler stderrHandler, final CmdLine commandLine) {
        final OverthereProcess process = startProcess(commandLine);
        Thread stdoutReaderThread = null;
        Thread stderrReaderThread = null;
        final CountDownLatch latch = new CountDownLatch(2);
        try {
            Map<String, String> mdcContext =  MDC.getCopyOfContextMap();
            stdoutReaderThread = getThread("stdout", commandLine.toString(), stdoutHandler, process.getStdout(), latch, mdcContext);
            stdoutReaderThread.start();

            stderrReaderThread = getThread("stderr", commandLine.toString(), stderrHandler, process.getStderr(), latch, mdcContext);
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
            quietlyJoinThread(stdoutReaderThread);
            quietlyJoinThread(stderrReaderThread);
        }
    }

    private void quietlyJoinThread(final Thread thread) {
        if (thread != null) {
            try {
                // interrupt the thread in case it is stuck waiting for output that will never come
                thread.interrupt();
                thread.join();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Thread getThread(final String streamName, final String commandLine, final OverthereExecutionOutputHandler outputHandler, final InputStream stream, final CountDownLatch latch, final Map<String, String> mdcContext) {
        Thread t = new Thread(format("%s reader", streamName)) {
            @Override
            public void run() {
                Map<String, String> previous = MDC.getCopyOfContextMap();
                setMDCContext(mdcContext);
                StringBuilder lineBuffer = new StringBuilder();
                InputStreamReader stdoutReader = new InputStreamReader(stream);
                latch.countDown();
                try {
                    int cInt = stdoutReader.read();
                    while (cInt > -1) {
                        char c = (char) cInt;
                        outputHandler.handleChar(c);
                        if (c != '\r' && c != '\n') {
                            lineBuffer.append(c);
                        }
                        if (c == '\n') {
                            outputHandler.handleLine(lineBuffer.toString());
                            lineBuffer.setLength(0);
                        }
                        cInt = stdoutReader.read();
                    }
                } catch (Exception exc) {
                    logger.error(format("An exception occured reading %s while executing [%s] on %s", streamName, commandLine, this), exc);
                } finally {
                    closeQuietly(stdoutReader);
                    if (lineBuffer.length() > 0) {
                        outputHandler.handleLine(lineBuffer.toString());
                    }
                    setMDCContext(previous);
                }
            }

            private void setMDCContext(Map<String, String> context) {
                if (context == null) {
                    MDC.clear();
                } else {
                    MDC.setContextMap(context);
                }
            }
        };
        t.setDaemon(true);
        return t;
    }

    /**
     * Executes a command with its arguments.
     *
     * @param handler     the handler that will be invoked when the executed command generated output.
     * @param commandLine the command line to execute.
     * @return the exit value of the executed command. Usually 0 on successful execution.
     * @deprecated use {@link BaseOverthereConnection#execute(com.xebialabs.overthere.OverthereExecutionOutputHandler, com.xebialabs.overthere.OverthereExecutionOutputHandler, com.xebialabs.overthere.CmdLine)}
     */
    @Override
    public final int execute(final OverthereProcessOutputHandler handler, final CmdLine commandLine) {
        return execute(wrapStdout(handler), wrapStderr(handler), commandLine);
    }

    /**
     * Starts a command with its argument and returns control to the caller.
     *
     * @param commandLine the command line to execute.
     * @return an object representing the executing command or <tt>null</tt> if this is not supported by the host
     *         connection.
     */
    @Override
    public OverthereProcess startProcess(CmdLine commandLine) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BaseOverthereConnection that = (BaseOverthereConnection) o;

        return options.equals(that.options) && protocol.equals(that.protocol);
    }

    @Override
    public int hashCode() {
        int result = protocol.hashCode();
        result = 31 * result + options.hashCode();
        return result;
    }

    /**
     * Make sure that the connection is cleaned up. This will log error messages if the connection is collected before it is cleaned up.
     *
     * @throws Throwable
     */
    @Override
    protected void finalize() throws Throwable {
        if (isConnected) {
            logger.error(String.format("Connection [%s] was not closed, closing automatically.", this), openStack);
            closeQuietly(this);
        }
        super.finalize();
    }
}
