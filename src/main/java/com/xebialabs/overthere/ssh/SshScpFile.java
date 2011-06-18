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

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.CmdLine.build;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.StringTokenizer;

import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

/**
 * A file on a host connected through SSH w/ SCP.
 */
class SshScpFile extends SshFile<SshScpConnection> {

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
	 * @param file
	 *            the file
	 * @return the information about the file, never <code>null</code>.
	 * @throws RuntimeIOException
	 *             if an I/O exception occurs
	 */
	public LsResults getFileInfo() throws RuntimeIOException {
		LsResults results = new LsResults();
		CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		int errno = executeCommand(capturedOutput, CmdLine.build("ls", "-ld", getPath()));
		if (errno == 0) {
			results.exists = true;
			if (capturedOutput.getOutputLines().size() > 0) {
				// parse ls results
				String outputLine = capturedOutput.getOutputLines().get(0);
				if (logger.isDebugEnabled())
					logger.debug("ls output = " + outputLine);
				StringTokenizer outputTokens = new StringTokenizer(outputLine);
				if (outputTokens.countTokens() < 5) {
					throw new RuntimeIOException("ls -ld " + getPath() + " returned output that contains less than the expected 5 tokens");
				}
				String permissions = outputTokens.nextToken();
				@SuppressWarnings("unused")
				String inodelinkes = outputTokens.nextToken();
				@SuppressWarnings("unused")
				String owner = outputTokens.nextToken();
				@SuppressWarnings("unused")
				String group = outputTokens.nextToken();
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
			}
		} else {
			results.exists = false;
		}

		if (logger.isDebugEnabled())
			logger.debug("Listed file " + this + ": exists=" + results.exists + ", isDirectory=" + results.isDirectory + ", length=" + results.length
			        + ", canRead=" + results.canRead + ", canWrite=" + results.canWrite + ", canExecute=" + results.canExecute);
		return results;
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
        logger.debug("Opening SCP input stream to read from file {}", this);

        try {
            final File tempFile = File.createTempFile("scp_download", ".tmp");
            tempFile.deleteOnExit();
            connection.getSshClient().newSCPFileTransfer().download(getPath(), tempFile.getPath());
            return new FileInputStream(tempFile) {
                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                    } finally {
                        tempFile.delete();
                    }

                }
            };
        } catch (IOException e) {
            throw new RuntimeIOException("Could not open " + this + " for reading", e);
        }
	}

	@Override
	public OutputStream getOutputStream() throws RuntimeIOException {
		logger.debug("Opening SCP output stream to write to file {}", this);

        try {
            final File tempFile = File.createTempFile("scp_upload", ".tmp");
            tempFile.deleteOnExit();
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
                    try {
                        connection.getSshClient().newSCPFileTransfer().upload(tempFile.getPath(), getPath());
                    } finally {
                        tempFile.delete();
                    }
                }
            };
        } catch (IOException e) {
            throw new RuntimeIOException("Could not open " + this + " for reading", e);
        }
	}

	@Override
	public List<OverthereFile> listFiles() {
		logger.debug("Listing directory {}", this);

		CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		// Yes, this *is* meant to be 'el es minus one'! Each file should go one a separate line, even if we create a pseudo-tty. Long format is NOT what we
		// want here.
		int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), build("ls", "-1", getPath()));
		if (errno != 0) {
			throw new RuntimeIOException("Cannot list directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}

		List<OverthereFile> files = newArrayList();
		for (String lsLine : capturedOutput.getOutputLines()) {
			files.add(connection.getFile(this, lsLine));
		}

		return files;
	}

	public void mkdir() {
		logger.debug("Creating directory {}", this);

		mkdir(new String[0]);
	}

	public void mkdirs() {
		logger.debug("Creating directories {}", this);

		mkdir("-p");
	}

	protected void mkdir(String... mkdirOptions) throws RuntimeIOException {
		CmdLine commandLine = CmdLine.build("mkdir");
		for (String opt : mkdirOptions) {
			commandLine.addArgument(opt);
		}
		commandLine.addArgument(getPath());

		CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), commandLine);
		if (errno != 0) {
			throw new RuntimeIOException("Cannot create directory or -ies " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
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
				CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
				int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), CmdLine.build("mv", getPath(), sshScpDestFile.getPath()));
				if (errno != 0) {
					throw new RuntimeIOException("Cannot rename file/directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
				}
			} else {
				throw new RuntimeIOException("Cannot rename ssh_scp file/directory " + this + " to ssh_scp file/directory " + dest + " because it is in a different connection");
			}
		} else {
			throw new RuntimeIOException("Cannot rename ssh_scp file/directory " + this + " to non-ssh_scp file/directory " + dest);
		}
	}

	@Override
	protected void deleteDirectory() {
		logger.debug("Deleting directory {}", this);

		CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), CmdLine.build("rmdir", getPath()));
		if (errno != 0) {
			throw new RuntimeIOException("Cannot delete directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
	}

	@Override
	protected void deleteFile() {
		logger.debug("Deleting file {}", this);

		CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), CmdLine.build("rm", "-f", getPath()));
		if (errno != 0) {
			throw new RuntimeIOException("Cannot delete file " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
	}

	@Override
	public void deleteRecursively() throws RuntimeIOException {
		logger.debug("Recursively deleting file or directory {}", this);

			CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), CmdLine.build("rm", "-rf", getPath()));
		if (errno != 0) {
			throw new RuntimeIOException("Cannot recursively delete file or directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
	}

	@Override
    protected void copyFrom(OverthereFile source) {
		logger.debug("Copying file or directory {} to {}", source, this);
		try {
	        connection.getSshClient().newSCPFileTransfer().newSCPUploadClient().copy(new OverthereFileLocalSourceFile(source), getPath());
        } catch (IOException e) {
        	throw new RuntimeIOException("Cannot copy " + source + " to " + this, e);
        }
    }

	private static class OverthereFileLocalSourceFile implements LocalSourceFile {

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
	         for(OverthereFile each: f.listFiles()) {
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
