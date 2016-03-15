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

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;
import net.schmizz.sshj.xfer.scp.SCPUploadClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xebialabs.overthere.CmdLine.build;
import static com.xebialabs.overthere.ssh.SshConnection.NOCD_PSEUDO_COMMAND;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingErrorHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingOutputHandler;
import static com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler.multiHandler;
import static com.xebialabs.overthere.util.NullOverthereExecutionOutputHandler.swallow;
import static java.lang.String.format;

/**
 * A file on a host connected through SSH w/ SCP.
 */
class SshScpFile extends SshFile<SshScpConnection> {

    private static final String PERMISSIONS_TOKEN_PATTERN = ".*?([dl\\-]([r\\-][w\\-][xsStT\\-]){3}[@\\.\\+]*)";

    private static Pattern permissionsTokenPattern = Pattern.compile(PERMISSIONS_TOKEN_PATTERN);

    /**
     * Constructs an SshScpOverthereFile
     *
     * @param connection the connection to the host
     * @param remotePath the path of the file on the host
     */
    public SshScpFile(SshScpConnection connection, String remotePath) {
        super(connection, remotePath);
    }

    @Override
    public boolean exists() {
        return getFileInfo().exists;
    }

    @Override
    public boolean canRead() {
        return getFileInfo().canRead;
    }

    @Override
    public boolean canWrite() {
        return getFileInfo().canWrite;
    }

    @Override
    public boolean canExecute() {
        return getFileInfo().canExecute;
    }

    @Override
    public boolean isFile() {
        return getFileInfo().isFile;
    }

    @Override
    public boolean isDirectory() {
        return getFileInfo().isDirectory;
    }

    @Override
    public long lastModified() {
        // FIXME: Implement by parsing the date output of `ls -l`
        throw new UnsupportedOperationException();
    }

    @Override
    public long length() {
        return getFileInfo().length;
    }

    /**
     * Gets information about the file by executing "ls -ld" on it.
     *
     * @return the information about the file, never <code>null</code>.
     * @throws RuntimeIOException if an I/O exception occurs
     */
    public LsResults getFileInfo() throws RuntimeIOException {
        logger.debug("Retrieving file info of {}", this);

        CmdLine lsCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND).addTemplatedFragment(connection.getFileInfoCommand, getPath());
        LsResults results = new LsResults();
        CapturingOverthereExecutionOutputHandler capturedOutput = capturingHandler();
        int errno = executeCommand(capturedOutput, swallow(), lsCmdLine);
        if (errno == 0) {
            for (int i = capturedOutput.getOutputLines().size() - 1; i >= 0; i--) {
                if (parseLsOutputLine(results, capturedOutput.getOutputLines().get(i))) {
                    results.exists = true;
                    break;
                }
            }

            if (!results.exists) {
                throw new RuntimeIOException("ls -ld " + getPath() + " returned " + errno + " but its output is unparseable: " + capturedOutput.getOutput());
            }
        } else {
            results.exists = false;
        }

        logger.debug("Listed file {}: exists={}, isDirectory={}, length={}, canRead={}, canWrite={}, canExecute={}", new Object[]{this, results.exists,
                results.isDirectory, results.length
                , results.canRead, results.canWrite, results.canExecute});

        return results;
    }

    protected boolean parseLsOutputLine(LsResults results, String outputLine) {
        StringTokenizer outputTokens = new StringTokenizer(outputLine);
        if (outputTokens.countTokens() < 5) {
            logger.debug("Not parsing ls output line [{}] because it has less than 5 tokens", outputLine);
            return false;
        }

        String permissions = outputTokens.nextToken();
        Matcher matcher = permissionsTokenPattern.matcher(permissions);
        if (!matcher.matches()) {
            logger.debug("Not parsing ls output line [{}] because it the first token does not match the pattern for permissions [" + PERMISSIONS_TOKEN_PATTERN
                    + "]", outputLine);
            return false;
        } else {
            permissions = matcher.group(1);
        }

        logger.debug("Parsing ls output line [{}]", outputLine);
        outputTokens.nextToken(); // inodelinks
        outputTokens.nextToken(); // owner
        outputTokens.nextToken(); // group
        String size = outputTokens.nextToken();

        results.isFile = permissions.length() >= 1 && permissions.charAt(0) == '-';
        results.isDirectory = permissions.length() >= 1 && permissions.charAt(0) == 'd';
        results.canRead = permissions.length() >= 2 && permissions.charAt(1) == 'r';
        results.canWrite = permissions.length() >= 3 && permissions.charAt(2) == 'w';
        results.canExecute = permissions.length() >= 4 && (permissions.charAt(3) == 'x' || permissions.charAt(3) == 's' || permissions.charAt(3) == 't');
        try {
            results.length = Integer.parseInt(size);
        } catch (NumberFormatException exc) {
            logger.warn("Cannot parse length of " + this.getPath() + " from ls output: " + outputLine + ". Length will be reported as -1.", exc);
        }
        return true;
    }

    /**
     * Holds results of an ls call
     */
    public static class LsResults {
        public boolean exists;
        public boolean isFile;
        public boolean isDirectory;
        public long length = -1;

        public boolean canRead;
        public boolean canWrite;
        public boolean canExecute;
    }

    @Override
    public InputStream getInputStream() throws RuntimeIOException {
        try {
            final File tempFile = File.createTempFile("scp_download", ".tmp");
            tempFile.deleteOnExit();

            logger.debug("Downloading contents of {} to temporary file {}", this, tempFile);
            connection.getSshClient().newSCPFileTransfer().download(getPath(), tempFile.getPath());

            logger.debug("Opening input stream to temporary file {} to retrieve contents downloaded from {}. Temporary file will be deleted when the stream is closed", tempFile, this);
            return asBuffered(new FileInputStream(tempFile) {
                @Override
                public void close() throws IOException {
                    logger.debug("Closing input stream to temporary file {}", tempFile);
                    try {
                        super.close();
                    } finally {
                        logger.debug("Deleting temporary file {}", tempFile);
                        tempFile.delete();
                    }

                }
            });
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot open %s for reading: %s", this, exc.toString()), exc);
        }
    }

    @Override
    public OutputStream getOutputStream() throws RuntimeIOException {
        try {
            final File tempFile = File.createTempFile("scp_upload", ".tmp");
            tempFile.deleteOnExit();

            logger.debug("Opening output stream to temporary file {} to store contents to be uploaded to {} when the stream is closed", tempFile, SshScpFile.this);
            return asBuffered(new FileOutputStream(tempFile) {
                @Override
                public void close() throws IOException {
                    logger.debug("Closing output stream to temporary file {}", tempFile);
                    try {
                        super.close();
                    } finally {
                        uploadAndDelete(tempFile);
                    }
                }

                private void uploadAndDelete(File tempFile) throws IOException {
                    logger.debug("Uploading contents of temporary file {} to to {}", tempFile, SshScpFile.this);
                    try {
                        connection.getSshClient().newSCPFileTransfer().upload(tempFile.getPath(), getPath());
                    } finally {
                        logger.debug("Deleting temporary file {}", tempFile);
                        tempFile.delete();
                    }
                }
            });
        } catch (IOException exc) {
            throw new RuntimeIOException(format("Cannot open %s for writing: %s", this, exc.toString()), exc);
        }
    }

    @Override
    public List<OverthereFile> listFiles() {
        logger.debug("Listing directory {}", this);

        CmdLine lsCmdLine = build(NOCD_PSEUDO_COMMAND).addTemplatedFragment(connection.listFilesCommand, getPath());

        CapturingOverthereExecutionOutputHandler capturedStdout = capturingHandler();
        CapturingOverthereExecutionOutputHandler capturedStderr = capturingHandler();
        int errno = executeCommand(multiHandler(loggingOutputHandler(logger), capturedStdout), multiHandler(loggingErrorHandler(logger), capturedStderr), lsCmdLine);
        if (errno != 0) {
            throw new RuntimeIOException("Cannot list directory " + this + ": " + capturedStderr.getOutput() + " (errno=" + errno + ")");
        }

        List<OverthereFile> files = new ArrayList<OverthereFile>();
        for (String lsLine : capturedStdout.getOutputLines()) {
            // Filter out the '.' and '..'
            if (!(".".equals(lsLine) || "..".equals(lsLine))) {
                files.add(connection.getFile(this, lsLine));
            }
        }

        return files;
    }

    @Override
    public void mkdir() {
        logger.debug("Creating directory {}", this);

        mkdir(connection.mkdirCommand);
    }

    @Override
    public void mkdirs() {
        logger.debug("Creating directories {}", this);

        mkdir(connection.mkdirsCommand);
    }

    protected void mkdir(String command) throws RuntimeIOException {
        CmdLine mkdirCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND).addTemplatedFragment(command, getPath());
        executeAndThrowOnErrorCode(mkdirCmdLine, "Cannot create directory or -ies " + this);

        if (logger.isDebugEnabled()) {
            logger.debug("Created directory " + this + " (using command: " + command + ")");
        }
    }

    @Override
    public void renameTo(OverthereFile dest) {
        logger.debug("Renaming {} to {}", this, dest);

        if (dest instanceof SshScpFile) {
            SshScpFile sshScpDestFile = (SshScpFile) dest;
            if (sshScpDestFile.getConnection() == getConnection()) {
                CmdLine mvCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND).addTemplatedFragment(connection.renameToCommand, getPath(), sshScpDestFile.getPath());
                executeAndThrowOnErrorCode(mvCmdLine, "Cannot rename file/directory " + this);
            } else {
                throw new RuntimeIOException("Cannot rename " + connection.protocolAndConnectionType + " file/directory " + this
                        + " to file/directory " + dest + " because it is in a different connection");
            }
        } else {
            throw new RuntimeIOException("Cannot rename " + connection.protocolAndConnectionType + " file/directory " + this
                    + " to non-" + connection.protocolAndConnectionType + " file/directory " + dest);
        }
    }

    @Override
    public void setExecutable(boolean executable) {
        logger.debug("Setting execute permission on {} to {}", this, executable);

        CmdLine chmodCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND).addTemplatedFragment(executable ? connection.setExecutableCommand : connection.setNotExecutableCommand, getPath());
        executeAndThrowOnErrorCode(chmodCmdLine, "Cannot set execute permission on file " + this + " to " + executable);
    }

    @Override
    protected void deleteDirectory() {
        logger.debug("Deleting directory {}", this);

        CmdLine rmdirCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND).addTemplatedFragment(connection.deleteDirectoryCommand, getPath());
        executeAndThrowOnErrorCode(rmdirCmdLine, "Cannot delete directory " + this);
    }

    @Override
    protected void deleteFile() {
        logger.debug("Deleting file {}", this);

        CmdLine rmCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND).addTemplatedFragment(connection.deleteFileCommand, getPath());
        executeAndThrowOnErrorCode(rmCmdLine, "Cannot delete file " + this);
    }

    @Override
    public void deleteRecursively() throws RuntimeIOException {
        logger.debug("Recursively deleting file or directory {}", this);

        CmdLine rmCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND).addTemplatedFragment(connection.deleteRecursivelyCommand, getPath());
        executeAndThrowOnErrorCode(rmCmdLine, "Cannot recursively delete file or directory " + this);
    }

    @Override
    protected void copyFrom(OverthereFile source) {
        logger.debug("Copying file or directory {} to {}", source, this);

        SCPUploadClient uploadClient = connection.getSshClient().newSCPFileTransfer().newSCPUploadClient();

        try {
            if (source.isDirectory() && this.exists()) {
                for (OverthereFile sourceFile : source.listFiles()) {
                    uploadClient.copy(new OverthereFileLocalSourceFile(sourceFile), getPath());
                }
            } else {
                uploadClient.copy(new OverthereFileLocalSourceFile(source), getPath());
            }
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot copy " + source + " to " + this + ": " + e.toString(), e);
        }
    }

    private void executeAndThrowOnErrorCode(CmdLine mkdirCmdLine, String message) {
        CapturingOverthereExecutionOutputHandler capturedStderr = capturingHandler();
        int errno = executeCommand(loggingOutputHandler(logger), multiHandler(loggingErrorHandler(logger), capturedStderr), mkdirCmdLine);
        if (errno != 0) {
            throw new RuntimeIOException(format("%s: %s (errno=%d)", message, capturedStderr.getOutput(), errno));
        }
    }

    private static Logger logger = LoggerFactory.getLogger(SshScpFile.class);

}
