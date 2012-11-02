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
package com.xebialabs.overthere.ssh;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;
import net.schmizz.sshj.xfer.scp.SCPUploadClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.CmdLine.build;
import static com.xebialabs.overthere.ssh.SshConnection.NOCD_PSEUDO_COMMAND;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;
import static java.lang.String.format;

/**
 * A file on a host connected through SSH w/ SCP.
 */
class SshScpFile extends SshFile<SshScpConnection> {

    private static final String PERMISSIONS_TOKEN_PATTERN = "[d\\-]([r\\-][w\\-][xst\\-]){3}[\\@\\.\\+]*";

    private static Pattern permissionsTokenPattern = Pattern.compile(PERMISSIONS_TOKEN_PATTERN);

    /**
     * Constructs an SshScpOverthereFile
     *
     * @param connection
     *            the connection to the host
     * @param remotePath
     *            the path of the file on the host
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
     * @throws RuntimeIOException
     *             if an I/O exception occurs
     */
    public LsResults getFileInfo() throws RuntimeIOException {
        CmdLine lsCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND, "ls", "-ld", getPath());

        LsResults results = new LsResults();
        CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
        int errno = executeCommand(capturedOutput, lsCmdLine);
        if (errno == 0) {
            for (int i = capturedOutput.getOutputLines().size() - 1; i >= 0; i--) {
                if (parseLsOutputLine(results, capturedOutput.getOutputLines().get(i))) {
                    results.exists = true;
                    break;
                }
            }

            if (!results.exists) {
                throw new RuntimeIOException("ls -ld " + getPath() + " returned unparseable output: " + capturedOutput.getOutput());
            }
        } else {
            results.exists = false;
        }

        logger.debug("Listed file [{}]: exists={}, isDirectory={}, length={}, canRead={}, canWrite={}, canExecute={}", new Object[] { this, results.exists,
            results.isDirectory, results.length
            , results.canRead, results.canWrite, results.canExecute });

        return results;
    }

    protected boolean parseLsOutputLine(LsResults results, String outputLine) {
        StringTokenizer outputTokens = new StringTokenizer(outputLine);
        if (outputTokens.countTokens() < 5) {
            logger.debug("Not parsing ls output line [%s] because it has less than 5 tokens", outputLine);
            return false;
        }

        String permissions = outputTokens.nextToken();
        if (!permissionsTokenPattern.matcher(permissions).matches()) {
            logger.debug("Not parsing ls output line [%s] because it the first token does not match the pattern for permissions [" + PERMISSIONS_TOKEN_PATTERN
                + "]", outputLine);
            return false;
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
        results.canExecute = permissions.length() >= 4 && permissions.charAt(3) == 'x';
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

            logger.debug(
                "Opening input stream to temporary file {} to retrieve contents download from {}. Temporary file will be deleted when the stream is closed",
                tempFile, this);
            return new FileInputStream(tempFile) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        logger.debug("Removing temporary file {}", tempFile);
                        tempFile.delete();
                    }

                }
            };
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot open " + this + " for reading: " + e.toString(), e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws RuntimeIOException {
        try {
            final File tempFile = File.createTempFile("scp_upload", ".tmp");
            tempFile.deleteOnExit();

            logger.debug("Opening output stream to temporary file {} to store contents to be uploaded to {} when the stream is closed", tempFile, this);
            return new FileOutputStream(tempFile) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        uploadAndDelete(tempFile);
                    }
                }

                private void uploadAndDelete(File tempFile) throws IOException {
                    logger.debug("Uploading contents of temporary file {} to to {}", tempFile, this);
                    try {
                        connection.getSshClient().newSCPFileTransfer().upload(tempFile.getPath(), getPath());
                    } finally {
                        logger.debug("Removing temporary file {}", tempFile);
                        tempFile.delete();
                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeIOException("Cannot open " + this + " for reading: " + e.toString(), e);
        }
    }

    @Override
    public List<OverthereFile> listFiles() {
        logger.debug("Listing directory {}", this);

        // Yes, this *is* meant to be 'el es minus one'! Each file should go one a separate line, even if we create a
        // pseudo-tty. Long format is NOT what we want here.
        CmdLine lsCmdLine = build(NOCD_PSEUDO_COMMAND, "ls", "-1", getPath());

        CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
        int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), lsCmdLine);
        if (errno != 0) {
            throw new RuntimeIOException("Cannot list directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
        }

        List<OverthereFile> files = newArrayList();
        for (String lsLine : capturedOutput.getOutputLines()) {
            files.add(connection.getFile(this, lsLine));
        }

        return files;
    }

    @Override
    public void mkdir() {
        logger.debug("Creating directory {}", this);

        mkdir(new String[0]);
    }

    @Override
    public void mkdirs() {
        logger.debug("Creating directories {}", this);

        mkdir("-p");
    }

    protected void mkdir(String... mkdirOptions) throws RuntimeIOException {
        CmdLine mkdirCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND, "mkdir");
        for (String opt : mkdirOptions) {
            mkdirCmdLine.addArgument(opt);
        }
        mkdirCmdLine.addArgument(getPath());

        CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
        int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), mkdirCmdLine);
        if (errno != 0) {
            throw new RuntimeIOException(
                format("Cannot create directory or -ies %s: %s (errno=%d)", this, capturedOutput.getError(), errno));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Created directory " + this + " (with options:" + Joiner.on(' ').join(mkdirOptions));
        }
    }

    @Override
    public void renameTo(OverthereFile dest) {
        logger.debug("Renaming {} to {}", this, dest);

        if (dest instanceof SshScpFile) {
            SshScpFile sshScpDestFile = (SshScpFile) dest;
            if (sshScpDestFile.getConnection() == getConnection()) {
                CmdLine mvCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND, "mv", getPath(), sshScpDestFile.getPath());

                CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
                int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), mvCmdLine);
                if (errno != 0) {
                    throw new RuntimeIOException("Cannot rename file/directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
                }
            } else {
                throw new RuntimeIOException("Cannot rename :ssh:" + connection.sshConnectionType.toString().toLowerCase() + ": file/directory " + this
                    + " to file/directory "
                    + dest + " because it is in a different connection");
            }
        } else {
            throw new RuntimeIOException("Cannot rename :ssh:" + connection.sshConnectionType.toString().toLowerCase() + ": file/directory " + this
                + " to non-:ssh:"
                + connection.sshConnectionType.toString().toLowerCase() + ": file/directory " + dest);
        }
    }

    @Override
    public void setExecutable(boolean executable) {
        logger.debug("Setting execute permission on {} to {}", this, executable);

        CmdLine chmodCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND, "chmod", executable ? "a+x" : "a-x", getPath());

        CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
        int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), chmodCmdLine);
        if (errno != 0) {
            throw new RuntimeIOException("Cannot set execute permission on file " + this + " to " + executable + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
        }
    }

    @Override
    protected void deleteDirectory() {
        logger.debug("Deleting directory {}", this);

        CmdLine rmdirCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND, "rmdir", getPath());

        CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
        int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), rmdirCmdLine);
        if (errno != 0) {
            throw new RuntimeIOException("Cannot delete directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
        }
    }

    @Override
    protected void deleteFile() {
        logger.debug("Deleting file {}", this);

        CmdLine rmCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND, "rm", "-f", getPath());

        CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
        int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), rmCmdLine);
        if (errno != 0) {
            throw new RuntimeIOException("Cannot delete file " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
        }
    }

    @Override
    public void deleteRecursively() throws RuntimeIOException {
        logger.debug("Recursively deleting file or directory {}", this);

        CmdLine rmCmdLine = CmdLine.build(NOCD_PSEUDO_COMMAND, "rm", "-rf", getPath());

        CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
        int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), rmCmdLine);
        if (errno != 0) {
            throw new RuntimeIOException("Cannot recursively delete file or directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
        }
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

    protected static class OverthereFileLocalSourceFile implements LocalSourceFile {

        private OverthereFile f;

        public OverthereFileLocalSourceFile(OverthereFile f) {
            this.f = f;
        }

        @Override
        public String getName() {
            return f.getName();
        }

        @Override
        public long getLength() {
            return f.length();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return f.getInputStream();
        }

        @Override
        public int getPermissions() throws IOException {
            return f.isDirectory() ? 0755 : 0644;
        }

        @Override
        public boolean isFile() {
            return f.isFile();
        }

        @Override
        public boolean isDirectory() {
            return f.isDirectory();
        }

        @Override
        public Iterable<? extends LocalSourceFile> getChildren(LocalFileFilter filter) throws IOException {
            List<LocalSourceFile> files = newArrayList();
            for (OverthereFile each : f.listFiles()) {
                files.add(new OverthereFileLocalSourceFile(each));
            }
            return files;
        }

        @Override
        public boolean providesAtimeMtime() {
            return false;
        }

        @Override
        public long getLastAccessTime() throws IOException {
            return 0;
        }

        @Override
        public long getLastModifiedTime() throws IOException {
            return 0;
        }

    }

    private Logger logger = LoggerFactory.getLogger(SshScpFile.class);

}
