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
package com.xebialabs.overthere.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereExecutionOutputHandler;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;

import static com.xebialabs.overthere.ssh.SshConnection.NOCD_PSEUDO_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshElevatedUserConnection.NOELEVATION_PSEUDO_COMMAND;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingErrorHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingOutputHandler;
import static com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler.multiHandler;

/**
 * A file on a host connected through SSH w/ SU, SUDO or INTERACTIVE_SUDO.
 */
class SshElevatedUserFile extends SshScpFile {

    private static Logger logger = LoggerFactory.getLogger(SshElevatedUserFile.class);
    private boolean isTempFile;

    /**
     * Constructs a SshElevatedUserFile
     *
     * @param connection the connection connected to the host
     * @param remotePath the path of the file on the host
     * @param isTempFile is <code>true</code> if this is a temporary file; <code>false</code> otherwise
     */
    public SshElevatedUserFile(SshElevatedUserConnection connection, String remotePath, boolean isTempFile) {
        super(connection, remotePath);
        this.isTempFile = isTempFile;
    }

    @Override
    protected int executeCommand(OverthereExecutionOutputHandler outHandler, OverthereExecutionOutputHandler errHandler, CmdLine commandLine) {
        if (isTempFile) {
            commandLine = SshConnection.prefixWithPseudoCommand(commandLine, NOELEVATION_PSEUDO_COMMAND);
        }
        return super.executeCommand(outHandler, errHandler, commandLine);
    }

    @Override
    public OverthereFile getFile(String name) {
        SshElevatedUserFile f = (SshElevatedUserFile) super.getFile(name);
        f.isTempFile = this.isTempFile;
        return f;
    }

    @Override
    public OverthereFile getParentFile() {
        SshElevatedUserFile f = (SshElevatedUserFile) super.getParentFile();
        f.isTempFile = this.isTempFile;
        return f;
    }

    @Override
    public InputStream getInputStream() throws RuntimeIOException {
        if (isTempFile) {
            return super.getInputStream();
        } else {
            OverthereFile tempFile = connection.getTempFile(getName());
            copyToTempFile(tempFile);
            return tempFile.getInputStream();
        }
    }

    @Override
    public OutputStream getOutputStream() throws RuntimeIOException {
        if (isTempFile) {
            return super.getOutputStream();
        } else {
            logger.debug("Opening {} output stream to write to file {}", connection.protocolAndConnectionType, this);
            return new SshElevatedUserOutputStream(this, connection.getTempFile(getName()));
        }
    }

    @Override
    public void mkdir() throws RuntimeIOException {
        if (isTempFile) {
            logger.debug("Creating world-writable directory, with sticky bit (mode 01777)");
            mkdir(((SshElevatedUserConnection) connection).tempMkdirCommand);
        } else {
            super.mkdir();
        }
    }

    @Override
    public void mkdirs() throws RuntimeIOException {
        if (isTempFile) {
            logger.debug("Creating world-writable directories, with sticky bit (mode 01777)");
            mkdir(((SshElevatedUserConnection) connection).tempMkdirsCommand);
        } else {
            super.mkdirs();
        }
    }

    @Override
    protected void copyFrom(OverthereFile source) {
        if (isTempFile) {
            super.copyFrom(source);
            overrideUmask(this);
        } else {
            logger.debug("Copying file or directory {} to {}", source, this);
            OverthereFile tempFile = getConnection().getTempFile(getName());
            try {
                connection.getSshClient().newSCPFileTransfer().newSCPUploadClient().copy(new OverthereFileLocalSourceFile(source), tempFile.getPath());
            } catch (IOException e) {
                throw new RuntimeIOException("Cannot copy " + source + " to " + this, e);
            }
            overrideUmask(tempFile);
            copyFromTempFile(tempFile);
        }
    }

    @Override
    protected CmdLine postProcessShortCircuitCopyCommand(CmdLine cmdLine) {
        if(isTempFile) {
            return new CmdLine().addArgument(NOELEVATION_PSEUDO_COMMAND).add(cmdLine.getArguments());
        } else {
            return cmdLine;
        }
    }

    private void overrideUmask(OverthereFile remoteFile) {
        if (((SshElevatedUserConnection) connection).overrideUmask) {
            logger.debug("Overriding umask by recursively setting permissions on files and/or directories copied with scp to be readable and executable (if needed) by group and other");

            CmdLine chmodCmdLine = CmdLine.build(NOELEVATION_PSEUDO_COMMAND, NOCD_PSEUDO_COMMAND)
                    .addTemplatedFragment(((SshElevatedUserConnection) connection).overrideUmaskCommand, remoteFile.getPath());

            CapturingOverthereExecutionOutputHandler capturedOutput = capturingHandler();
            int errno = connection.execute(loggingOutputHandler(logger), multiHandler(loggingErrorHandler(logger), capturedOutput), chmodCmdLine);
            if (errno != 0) {
                throw new RuntimeIOException("Cannot set permissions on file " + this + " to go+rX: " + capturedOutput.getOutput() + " (errno=" + errno + ")");
            }
        }
    }

    void copyToTempFile(OverthereFile tempFile) {
        logger.debug("Copying actual file {} to temporary file {} before download", this, tempFile);

        CmdLine cpCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND)
                .addTemplatedFragment(((SshElevatedUserConnection) connection).copyToTempFileCommand, this.getPath(), tempFile.getPath());

        CapturingOverthereExecutionOutputHandler cpCapturedOutput = capturingHandler();
        int cpResult = getConnection().execute(multiHandler(loggingOutputHandler(logger), cpCapturedOutput), multiHandler(loggingErrorHandler(logger), cpCapturedOutput), cpCmdLine);
        if (cpResult != 0) {
            String errorMessage = cpCapturedOutput.getOutput();
            throw new RuntimeIOException("Cannot copy actual file " + this + " to temporary file " + tempFile + " before download: " + errorMessage);
        }

        CmdLine chmodCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND)
                .addTemplatedFragment(((SshElevatedUserConnection) connection).overrideUmaskCommand, tempFile.getPath());

        CapturingOverthereExecutionOutputHandler chmodCapturedOutput = capturingHandler();
        int chmodResult = getConnection().execute(multiHandler(loggingOutputHandler(logger), chmodCapturedOutput), multiHandler(loggingErrorHandler(logger), chmodCapturedOutput), chmodCmdLine);
        if (chmodResult != 0) {
            String errorMessage = chmodCapturedOutput.getOutput();
            throw new RuntimeIOException("Cannot grant group and other read and execute permissions (chmod -R go+rX) to file " + tempFile
                    + " before download: " + errorMessage);
        }
    }

    void copyFromTempFile(OverthereFile tempFile) {
        logger.debug("Copying temporary file {} to actual file {} after upload", tempFile, this);

        String targetPath = this.getPath();
        if (this.exists() && tempFile.isDirectory()) {
            targetPath = this.getParentFile().getPath();
        }

        CmdLine cpCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND)
                .addTemplatedFragment(((SshElevatedUserConnection) connection).copyFromTempFileCommand, tempFile.getPath(), targetPath);

        CapturingOverthereExecutionOutputHandler cpCapturedOutput = capturingHandler();
        int cpResult = getConnection().execute(multiHandler(loggingOutputHandler(logger), cpCapturedOutput), multiHandler(loggingErrorHandler(logger), cpCapturedOutput), cpCmdLine);

        if (cpResult != 0) {
            String errorMessage = cpCapturedOutput.getOutput();
            throw new RuntimeIOException("Cannot copy temporary file " + tempFile + " to actual file " + this + " after upload: " + errorMessage);
        }
    }

    boolean isTempFile() {
        return isTempFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SshElevatedUserFile)) {
            return false;
        }
        return super.equals(obj) && isTempFile == ((SshElevatedUserFile) obj).isTempFile;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Boolean.valueOf(isTempFile).hashCode();
    }

}
