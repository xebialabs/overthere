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
import static com.xebialabs.overthere.spi.OverthereConnectionUtils.getFileInfo;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

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
class SshScpOverthereFile extends SshOverthereFile {

	/**
	 * Constructs an SshScpOverthereFile
	 * 
	 * @param connection
	 *            the connection to the host
	 * @param remotePath
	 *            the path of the file on the host
	 */
	public SshScpOverthereFile(SshOverthereConnection connection, String remotePath) {
		super(connection, remotePath);
	}

	@Override
	public boolean exists() {
		return getFileInfo(this).exists;
	}

	@Override
	public boolean isDirectory() {
		return getFileInfo(this).isDirectory;
	}

	@Override
	public long lastModified() {
		// FIXME: Implement by parsing the date output of `ls -l`
		throw new UnsupportedOperationException();
	}

	@Override
	public long length() {
		return getFileInfo(this).length;
	}

	@Override
	public boolean canRead() {
		return getFileInfo(this).canRead;
	}

	@Override
	public boolean canWrite() {
		return getFileInfo(this).canWrite;
	}

	@Override
	public boolean canExecute() {
		return getFileInfo(this).canExecute;
	}

	@Override
	public List<OverthereFile> listFiles() {
		if (logger.isDebugEnabled())
			logger.debug("Listing directory " + this);

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
		if (logger.isDebugEnabled())
			logger.debug("Creating directory " + this);

		mkdir(new String[0]);
	}

	public void mkdirs() {
		if (logger.isDebugEnabled())
			logger.debug("Creating directories " + this);

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
		if (logger.isDebugEnabled())
			logger.debug("Renaming " + this + " to " + dest);

		if (dest instanceof SshScpOverthereFile) {
			SshScpOverthereFile sshScpDestFile = (SshScpOverthereFile) dest;
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
		if (logger.isDebugEnabled())
			logger.debug("Deleting directory " + this);

		CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), CmdLine.build("rmdir", getPath()));
		if (errno != 0) {
			throw new RuntimeIOException("Cannot delete directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
	}

	@Override
	protected void deleteFile() {
		if (logger.isDebugEnabled())
			logger.debug("Deleting file " + this);

		CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), CmdLine.build("rm", "-f", getPath()));
		if (errno != 0) {
			throw new RuntimeIOException("Cannot delete file " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
	}

	@Override
	public void deleteRecursively() throws RuntimeIOException {
		if (logger.isDebugEnabled())
			logger.debug("Recursively deleting file/directory " + this);

			CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		int errno = executeCommand(multiHandler(loggingHandler(logger), capturedOutput), CmdLine.build("rm", "-rf", getPath()));
		if (errno != 0) {
			throw new RuntimeIOException("Cannot recursively delete directory " + this + ": " + capturedOutput.getError() + " (errno=" + errno + ")");
		}
	}

	@Override
	public InputStream getInputStream() throws RuntimeIOException {
		if (logger.isDebugEnabled())
			logger.debug("Opening SCP input stream to read from file " + this);

		return new SshScpInputStream(this);
	}

	@Override
	public OutputStream getOutputStream(long length) throws RuntimeIOException {
		if (logger.isDebugEnabled())
			logger.debug("Opening SCP output stream to write to file " + this);

		return new SshScpOutputStream(this, length);
	}

	private Logger logger = LoggerFactory.getLogger(SshScpOverthereFile.class);

}
