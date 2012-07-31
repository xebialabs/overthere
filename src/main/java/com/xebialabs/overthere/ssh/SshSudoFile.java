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
package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.xebialabs.overthere.CmdLine.build;
import static com.xebialabs.overthere.ssh.SshConnection.NOCD_PSEUDO_COMMAND;
import static com.xebialabs.overthere.ssh.SshSudoConnection.NOSUDO_PSEUDO_COMMAND;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;

/**
 * A file on a host connected through SSH w/ SUDO.
 */
class SshSudoFile extends SshScpFile {

    private boolean isTempFile;

    /**
     * Constructs a SshSudoHostFile
     * 
     * @param connection
     *            the connection connected to the host
     * @param remotePath
     *            the path of the file on the host
     * @param isTempFile
     *            is <code>true</code> if this is a temporary file; <code>false</code> otherwise
     */
    public SshSudoFile(SshSudoConnection connection, String remotePath, boolean isTempFile) {
        super(connection, remotePath);
        this.isTempFile = isTempFile;
    }

    @Override
    protected int executeCommand(OverthereProcessOutputHandler handler, CmdLine commandLine) {
        if (isTempFile) {
            commandLine = SshConnection.prefixWithPseudoCommand(commandLine, NOSUDO_PSEUDO_COMMAND);
        }
        return super.executeCommand(handler, commandLine);
    }

    @Override
    public OverthereFile getFile(String name) {
        SshSudoFile f = (SshSudoFile) super.getFile(name);
        f.isTempFile = this.isTempFile;
        return f;
    }

    @Override
    public OverthereFile getParentFile() {
        SshSudoFile f = (SshSudoFile) super.getParentFile();
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
            logger.debug("Opening ssh:sudo: output stream to write to file {}", this);
            return new SshSudoOutputStream(this, connection.getTempFile(getName()));
        }
    }

    @Override
    public void mkdir() throws RuntimeIOException {
        if (isTempFile) {
            /*
             * For SUDO access, temporary dirs also need to be writable to the connecting user, otherwise an SCP copy
             * will fail. 1777 is world writable with the sticky bit set.
             */
            logger.debug("Creating world-writable directory, with sticky bit (mode 01777)");
            mkdir("-m", "1777");
        } else {
            super.mkdir();
        }
    }

    @Override
    public void mkdirs() throws RuntimeIOException {
        if (isTempFile) {
            /*
             * For SUDO access, temporary dirs also need to be writable to the connecting user, otherwise an SCP copy
             * will fail. 1777 is world writable with the sticky bit set.
             */
            logger.debug("Creating world-writable directories, with sticky bit (mode 01777)");
            mkdir("-p", "-m", "1777");
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
            copyfromTempFile(tempFile);
        }
    }

    private void overrideUmask(OverthereFile remoteFile) {
        if (((SshSudoConnection) connection).sudoOverrideUmask) {
            logger
                .debug("Overriding umask by recursively setting permissions on files and/or directories copied with scp to be readable and executable (if needed) by group and other");
            CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
            int errno = connection.execute(multiHandler(loggingHandler(logger), capturedOutput),
                CmdLine.build(NOSUDO_PSEUDO_COMMAND, NOCD_PSEUDO_COMMAND, "chmod", "-R", "go+rX", remoteFile.getPath()));
            if (errno != 0) {
                throw new RuntimeIOException("Cannot set permissions on file " + this + " to go+rX: " + capturedOutput.getError() + " (errno=" + errno + ")");
            }
        }
    }

    void copyToTempFile(OverthereFile tempFile) {
        logger.debug("Copying actual file {} to temporary file {} before download", this, tempFile);

        CapturingOverthereProcessOutputHandler cpCapturedOutput = CapturingOverthereProcessOutputHandler.capturingHandler();
        int cpResult = getConnection().execute(multiHandler(loggingHandler(logger), cpCapturedOutput),
            build(NOCD_PSEUDO_COMMAND, "cp", "-pr", this.getPath(), tempFile.getPath()));
        if (cpResult != 0) {
            String errorMessage = cpCapturedOutput.getAll();
            throw new RuntimeIOException("Cannot copy actual file " + this + " to temporary file " + tempFile + " before download: " + errorMessage);
        }

        CapturingOverthereProcessOutputHandler chmodCapturedOutput = capturingHandler();
        int chmodResult = getConnection().execute(multiHandler(loggingHandler(logger), chmodCapturedOutput),
            CmdLine.build(NOCD_PSEUDO_COMMAND, "chmod", "-R", "go+rX", tempFile.getPath()));
        if (chmodResult != 0) {
            String errorMessage = chmodCapturedOutput.getAll();
            throw new RuntimeIOException("Cannot grant group and other read and execute permissions (chmod -R go+rX) to file " + tempFile
                + " before download: " + errorMessage);
        }
    }

    void copyfromTempFile(OverthereFile tempFile) {
        logger.debug("Copying temporary file {} to actual file {} after upload", tempFile, this);

        CapturingOverthereProcessOutputHandler cpCapturedOutput = capturingHandler();
        CmdLine cmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND, "cp", "-pr");
        String sourcePath = tempFile.getPath();
        if (this.exists() && tempFile.isDirectory()) {
            sourcePath += "/*"; // FIXME: This will not copy hidden files!
        }
        cmdLine.addRaw(sourcePath);
        cmdLine.addArgument(this.getPath());

        int cpResult = getConnection().execute(multiHandler(loggingHandler(logger), cpCapturedOutput), cmdLine);
        if (cpResult != 0) {
            String errorMessage = cpCapturedOutput.getAll();
            throw new RuntimeIOException("Cannot copy temporary file " + tempFile + " to actual file " + this + " after upload: " + errorMessage);
        }
    }

    private Logger logger = LoggerFactory.getLogger(SshSudoFile.class);

}
