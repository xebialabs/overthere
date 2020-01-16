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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;
import com.xebialabs.overthere.util.OverthereFileCopier;

import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.ConnectionOptions.DIRECTORY_COPY_COMMAND_FOR_UNIX;
import static com.xebialabs.overthere.ConnectionOptions.DIRECTORY_COPY_COMMAND_FOR_UNIX_DEFAULT;
import static com.xebialabs.overthere.ConnectionOptions.DIRECTORY_COPY_COMMAND_FOR_WINDOWS;
import static com.xebialabs.overthere.ConnectionOptions.DIRECTORY_COPY_COMMAND_FOR_WINDOWS_DEFAULT;
import static com.xebialabs.overthere.ConnectionOptions.DIRECTORY_COPY_COMMAND_FOR_ZOS;
import static com.xebialabs.overthere.ConnectionOptions.DIRECTORY_COPY_COMMAND_FOR_ZOS_DEFAULT;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_UNIX;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_UNIX_DEFAULT;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_WINDOWS;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_WINDOWS_DEFAULT;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_ZOS;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_ZOS_DEFAULT;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingErrorHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingOutputHandler;
import static com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler.multiHandler;
import static java.lang.String.format;

/**
 * A file system object (file, directory, etc.) on a remote system that is accessible through an
 * {@link com.xebialabs.overthere.OverthereConnection}.
 */
public abstract class BaseOverthereFile<C extends BaseOverthereConnection> implements OverthereFile {

    protected C connection;

    protected BaseOverthereFile() {
        this.connection = null;
    }

    protected BaseOverthereFile(C connection) {
        this.connection = connection;
    }

    @Override
    public C getConnection() {
        return connection;
    }

    @Override
    public OverthereFile getFile(String child) {
        return getConnection().getFile(this, child);
    }

    @Override
    public void deleteRecursively() throws RuntimeIOException {
        if (isDirectory()) {
            RuntimeIOException accumulator = new RuntimeIOException("Cannot delete " + this + ", not all children are deleted.");
            for (OverthereFile each : listFiles()) {
                try {
                    each.deleteRecursively();
                } catch (RuntimeIOException rio) {
                    logger.warn("Unable to delete child {}. Continue...", each);
                    accumulator.addSuppressed(rio);
                }
            }
            Throwable[] suppressed = accumulator.getSuppressed();
            if (suppressed == null || suppressed.length == 0) {
                delete();
            } else {
                throw accumulator;
            }
        } else {
            delete();
        }
    }

    @Override
    public final void copyTo(final OverthereFile dest) {
        checkArgument(dest instanceof BaseOverthereFile<?>, "dest is not a subclass of BaseOverthereFile");

        if (getConnection().equals(dest.getConnection())) {
            ((BaseOverthereFile<?>) dest).shortCircuitCopyFrom(this);
        } else {
            ((BaseOverthereFile<?>) dest).copyFrom(this);
        }
    }

    protected void copyFrom(OverthereFile source) {
        OverthereFileCopier.copy(source, this);
    }

    protected void shortCircuitCopyFrom(OverthereFile source) {
        checkArgument(source.exists(), "Source file [%s] does not exist", source);

        boolean srcIsDir = source.isDirectory();
        if (exists()) {
            if (srcIsDir) {
                checkArgument(isDirectory(), "Cannot copy source directory [%s] to target file [%s]", source, this);
            } else {
                checkArgument(!isDirectory(), "Cannot copy source file [%s] to target directory [%s]", source, this);
            }
        } else {
            if (srcIsDir) {
                mkdir();
            }
        }

        String copyCommandTemplate;
        switch (source.getConnection().getHostOperatingSystem()) {
            case UNIX:
                if (srcIsDir) {
                    copyCommandTemplate = getConnection().getOptions().get(DIRECTORY_COPY_COMMAND_FOR_UNIX, DIRECTORY_COPY_COMMAND_FOR_UNIX_DEFAULT);
                } else {
                    copyCommandTemplate = getConnection().getOptions().get(FILE_COPY_COMMAND_FOR_UNIX, FILE_COPY_COMMAND_FOR_UNIX_DEFAULT);
                }
                break;
            case WINDOWS:
                if (srcIsDir) {
                    copyCommandTemplate = getConnection().getOptions().get(DIRECTORY_COPY_COMMAND_FOR_WINDOWS, DIRECTORY_COPY_COMMAND_FOR_WINDOWS_DEFAULT);
                } else {
                    copyCommandTemplate = getConnection().getOptions().get(FILE_COPY_COMMAND_FOR_WINDOWS, FILE_COPY_COMMAND_FOR_WINDOWS_DEFAULT);
                }
                break;
            case ZOS:
                if (srcIsDir) {
                    copyCommandTemplate = getConnection().getOptions().get(DIRECTORY_COPY_COMMAND_FOR_ZOS, DIRECTORY_COPY_COMMAND_FOR_ZOS_DEFAULT);
                } else {
                    copyCommandTemplate = getConnection().getOptions().get(FILE_COPY_COMMAND_FOR_ZOS, FILE_COPY_COMMAND_FOR_ZOS_DEFAULT);
                }
                break;
            default:
                throw new IllegalArgumentException(format("Unknown operating system [%s]", source.getConnection().getHostOperatingSystem()));
        }
        CmdLine cmdLine = postProcessShortCircuitCopyCommand(new CmdLine().addTemplatedFragment(copyCommandTemplate, source.getPath(), getPath()));

        CapturingOverthereExecutionOutputHandler capturedStderr = capturingHandler();
        int errno = source.getConnection().execute(loggingOutputHandler(logger), multiHandler(loggingErrorHandler(logger), capturedStderr), cmdLine);
        if (errno != 0) {
            throw new RuntimeIOException(format("Cannot copy [%s] to [%s] on [%s]: %s (errno=%d)", source.getPath(), getPath(), getConnection(), capturedStderr.getOutput(), errno));
        }
    }

    protected CmdLine postProcessShortCircuitCopyCommand(CmdLine cmd) {
        return cmd;
    }

    protected InputStream asBuffered(InputStream is) {
        if (is instanceof BufferedInputStream) {
            return is;
        }
        int streamBufferSize = getConnection().streamBufferSize;
        logger.debug("Using buffer of size [{}] for streaming from [{}]", streamBufferSize, this);
        return new BufferedInputStream(is, streamBufferSize);
    }

    protected OutputStream asBuffered(OutputStream os) {
        if (os instanceof BufferedOutputStream) {
            return os;
        }
        int streamBufferSize = getConnection().streamBufferSize;
        logger.debug("Using buffer of size [{}] for streaming to [{}]", streamBufferSize, this);
        return new BufferedOutputStream(os, getConnection().streamBufferSize);
    }

    /**
     * Subclasses MUST implement toString properly.
     */
    @Override
    public abstract String toString();

    private static final Logger logger = LoggerFactory.getLogger(BaseOverthereFile.class);

}
