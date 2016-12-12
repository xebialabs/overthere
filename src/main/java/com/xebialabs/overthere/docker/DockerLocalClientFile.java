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
package com.xebialabs.overthere.docker;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.local.LocalFile;
import com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler;

import static com.xebialabs.overthere.ConnectionOptions.DIRECTORY_COPY_COMMAND_FOR_UNIX;
import static com.xebialabs.overthere.ConnectionOptions.DIRECTORY_COPY_COMMAND_FOR_UNIX_DEFAULT;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_UNIX;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_UNIX_DEFAULT;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.util.CapturingOverthereExecutionOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingErrorHandler;
import static com.xebialabs.overthere.util.LoggingOverthereExecutionOutputHandler.loggingOutputHandler;
import static com.xebialabs.overthere.util.MultipleOverthereExecutionOutputHandler.multiHandler;
import static com.xebialabs.overthere.util.NullOverthereExecutionOutputHandler.swallow;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static java.lang.String.format;

public class DockerLocalClientFile extends DockerFile<DockerLocalClientConnection> {

    private List<String> pathComponents;

    private static final String PERMISSIONS_TOKEN_PATTERN = "[dl\\-]([r\\-][w\\-][xsStT\\-]){3}[@\\.\\+]*";

    private static Pattern permissionsTokenPattern = Pattern.compile(PERMISSIONS_TOKEN_PATTERN);


    protected DockerLocalClientFile(final DockerLocalClientConnection connection, String path) {
        super(connection);
        this.pathComponents = splitPath(path);


    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DockerLocalClientFile)) {
            return false;
        }

        return getPath().equals(((DockerLocalClientFile) obj).getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public String toString() {
        String p = getPath();
        if (p.length() >= 1 && p.charAt(0) == '/') {
            return getConnection() + p;
        } else {
            return getConnection() + "/" + p;
        }
    }

    @Override
    public String getPath() {
        return joinPath(pathComponents);
    }

    @Override
    public String getName() {
        if (pathComponents.isEmpty()) {
            return UNIX.getFileSeparator();
        } else {
            return pathComponents.get(pathComponents.size() - 1);
        }
    }

    @Override
    public OverthereFile getParentFile() {
        if (pathComponents.isEmpty()) {
            // The root path is its own parent.
            return this;
        }

        return connection.getFile(joinPath(pathComponents.subList(0, pathComponents.size() - 1)));
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

    @Override
    public boolean isHidden() {
        return getName().startsWith(".");
    }

    @Override
    public InputStream getInputStream() {
        return new DockerCpFileBackedInputStream();
    }

    @Override
    public OutputStream getOutputStream() {
        return new DockerShCatOutputStream();
    }

    @Override
    public void setExecutable(final boolean executable) {
        logger.debug("Setting execute permission on {} to {}", this, executable);

        CmdLine chmodCmdLine = new CmdLine().addTemplatedFragment(executable ? connection.setExecutableCommand : connection.setNotExecutableCommand, getPath());
        executeAndThrowOnErrorCode(chmodCmdLine, "Cannot set execute permission on file " + this + " to " + executable);

    }

    @Override
    public void delete() throws RuntimeIOException {
        if (exists()) {
            if (isDirectory()) {
                deleteDirectory();
            } else {
                deleteFile();
            }
        }
    }

    protected void deleteDirectory() {
        logger.debug("Deleting directory {}", this);

        CmdLine rmdirCmdLine = new CmdLine().addTemplatedFragment(connection.deleteDirectoryCommand, getPath());
        executeAndThrowOnErrorCode(rmdirCmdLine, "Cannot delete directory " + this);
    }

    protected void deleteFile() {
        logger.debug("Deleting file {}", this);

        CmdLine rmCmdLine = new CmdLine().addTemplatedFragment(connection.deleteFileCommand, getPath());
        executeAndThrowOnErrorCode(rmCmdLine, "Cannot delete file " + this);
    }

    @Override
    public void deleteRecursively() throws RuntimeIOException {
        logger.debug("Recursively deleting file or directory {}", this);

        CmdLine rmCmdLine = new CmdLine().addTemplatedFragment(connection.deleteRecursivelyCommand, getPath());
        executeAndThrowOnErrorCode(rmCmdLine, "Cannot recursively delete file or directory " + this);
    }


    @Override
    public List<OverthereFile> listFiles() {
        logger.debug("Listing directory {}", this);

        CmdLine lsCmdLine = new CmdLine().addTemplatedFragment(connection.listFilesCommand, getPath());

        CapturingOverthereExecutionOutputHandler capturedStdout = capturingHandler();
        CapturingOverthereExecutionOutputHandler capturedStderr = capturingHandler();
        int errno = connection.execute(multiHandler(loggingOutputHandler(logger), capturedStdout), multiHandler(loggingErrorHandler(logger), capturedStderr), lsCmdLine);
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
        CmdLine mkdirCmdLine = new CmdLine().addTemplatedFragment(command, getPath());
        executeAndThrowOnErrorCode(mkdirCmdLine, "Cannot create directory or -ies " + this);

        if (logger.isDebugEnabled()) {
            logger.debug("Created directory " + this + " (using command: " + command + ")");
        }
    }
    @Override
    public void renameTo(OverthereFile dest) {
        logger.debug("Renaming {} to {}", this, dest);

        if (dest instanceof DockerLocalClientFile) {
            DockerLocalClientFile sshScpDestFile = (DockerLocalClientFile) dest;
            if (sshScpDestFile.getConnection() == getConnection()) {
                CmdLine mvCmdLine = new CmdLine().addTemplatedFragment(connection.renameToCommand, getPath(), sshScpDestFile.getPath());
                executeAndThrowOnErrorCode(mvCmdLine, "Cannot rename file/directory " + this);
            } else {
                throw new RuntimeIOException("Cannot rename :docker:" + connection.dockerConnectionType.toString().toLowerCase() + ": file/directory " + this
                        + " to file/directory "
                        + dest + " because it is in a different connection");
            }
        } else {
            throw new RuntimeIOException("Cannot rename :ssh:" + connection.dockerConnectionType.toString().toLowerCase() + ": file/directory " + this
                    + " to non-:ssh:"
                    + connection.dockerConnectionType.toString().toLowerCase() + ": file/directory " + dest);
        }
    }

    public LsResults getFileInfo() throws RuntimeIOException {
        logger.debug("Retrieving file info of {}", this);

        CmdLine lsCmdLine = new CmdLine().addTemplatedFragment(connection.getFileInfoCommand, getPath());
        LsResults results = new LsResults();
        CapturingOverthereExecutionOutputHandler capturedOutput = capturingHandler();
        int errno = connection.execute(capturedOutput, swallow(), lsCmdLine);
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
        if (!permissionsTokenPattern.matcher(permissions).matches()) {
            logger.debug("Not parsing ls output line [{}] because it the first token does not match the pattern for permissions [" + PERMISSIONS_TOKEN_PATTERN
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

    private void executeAndThrowOnErrorCode(CmdLine mkdirCmdLine, String message) {
        CapturingOverthereExecutionOutputHandler capturedStderr = capturingHandler();
        int errno = connection.execute(loggingOutputHandler(logger), multiHandler(loggingErrorHandler(logger), capturedStderr), mkdirCmdLine);
        if (errno != 0) {
            throw new RuntimeIOException(format("%s: %s (errno=%d)", message, capturedStderr.getOutput(), errno));
        }
    }


    private class DockerCpFileBackedInputStream extends InputStream {
        private OverthereConnection localTempConn = LocalConnection.getLocalConnection();
        private LocalFile localTempDir = null;
        private InputStream in = null;

        public DockerCpFileBackedInputStream() {
            localTempDir = (LocalFile) localTempConn.getTempFile("temp_download");
            localTempDir.mkdir();
            CmdLine cmd = CmdLine.build("docker", "cp", connection.dockerContainer + ":" + getPath(), localTempDir.getPath());
            OverthereProcess p = connection.executeDocker(cmd);
            try {
                int rc = p.waitFor();
                if (rc != 0) {
                    localTempConn.close();
                    throw new RuntimeIOException("Failed to copy file from container. RC=" + rc + ". Commad " + cmd);
                }
            } catch (InterruptedException e) {
                throw new RuntimeIOException("Interrupted will copying file from container", e);
            }

            LocalFile file = (LocalFile) localTempDir.getFile(getName());
            try {
                in = new FileInputStream(file.getFile());
            } catch (FileNotFoundException e) {
                throw new RuntimeIOException(e);
            }
        }

        @Override
        public int read() throws IOException {
            if (in == null) {
                return -1;
            }
            return in.read();
        }

        @Override
        public int available() throws IOException {
            if (in != null) {
                return in.available();
            }
            return 0;
        }

        @Override
        public void close() throws IOException {
            super.close();
            in.close();
            localTempDir = null;
            localTempConn.close();
        }
    }

    @Override
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
        if (srcIsDir) {
            copyCommandTemplate = getConnection().getOptions().get(DIRECTORY_COPY_COMMAND_FOR_UNIX, DIRECTORY_COPY_COMMAND_FOR_UNIX_DEFAULT);
        } else {
            copyCommandTemplate = getConnection().getOptions().get(FILE_COPY_COMMAND_FOR_UNIX, FILE_COPY_COMMAND_FOR_UNIX_DEFAULT);
        }

        CmdLine cmdLine = connection.getDockerExecCmd();
        cmdLine.addArgument("sh");
        cmdLine.addArgument("-c");
        CmdLine cpCmdLine = new CmdLine().addTemplatedFragment(copyCommandTemplate, source.getPath(), getPath());
        cmdLine.addRaw(cpCmdLine.toCommandLine(UNIX, false));
        OverthereProcess process = connection.executeDocker(cmdLine);

        CapturingOverthereExecutionOutputHandler capturedStderr = capturingHandler();
        int errno = connection.attachToProcess(loggingOutputHandler(logger), multiHandler(loggingErrorHandler(logger), capturedStderr), cmdLine, process);
        if (errno != 0) {
            throw new RuntimeIOException(format("Cannot copy [%s] to [%s] on [%s]: %s (errno=%d)", source.getPath(), getPath(), getConnection(), capturedStderr.getOutput(), errno));
        }
    }

    private class DockerShCatOutputStream extends OutputStream {
        private final OverthereProcess process;
        private OutputStream out;
        DockerShCatOutputStream() {
            CmdLine writeCmd = CmdLine.build("docker", "exec", "-i", connection.dockerContainer, "/bin/sh", "-c");
            String escapePath = getPath();
            escapePath = escapePath.replace(" ","\\ ");
            writeCmd.addArgument("cat > " + escapePath);
            process = connection.executeDocker(writeCmd);
            out = process.getStdin();
        }

        @Override
        public void write(final int b) throws IOException {
            out.write(b);
        }

        @Override
        public void close() throws IOException {
            out.flush();
            super.close();
            out.close();
            logger.info("Waiting for Cat file process to end");
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                //do nothing
            }
            logger.info("Cat to file process ended");
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(DockerLocalClientFile.class);
}
