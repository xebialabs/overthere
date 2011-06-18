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

import static com.xebialabs.overthere.CmdLine.build;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcessOutputHandler;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

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
			return ((SshSudoConnection) connection).noSudoExecute(handler, commandLine);
		} else {
			return super.executeCommand(handler, commandLine);
		}
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
			copyHostFileToTempFile(tempFile);
			return tempFile.getInputStream();
		}
	}

	private void copyHostFileToTempFile(OverthereFile tempFile) {
		if (logger.isDebugEnabled()) {
			logger.debug("Copying " + this + " to " + tempFile + " for reading");
		}

		CapturingOverthereProcessOutputHandler capturedOutput = CapturingOverthereProcessOutputHandler.capturingHandler();
		int result = connection.execute(multiHandler(loggingHandler(logger), capturedOutput), build("cp", this.getPath(), tempFile.getPath()));
		if (result != 0) {
			String errorMessage = capturedOutput.getAll();
			throw new RuntimeIOException("Cannot copy " + this + " to " + tempFile + " for reading: " + errorMessage);
		}

		logger.info("Copied " + this + " to " + tempFile + " for reading");
	}

	@Override
	public OutputStream getOutputStream() throws RuntimeIOException {
		if (isTempFile) {
			return super.getOutputStream();
		} else {
			SshSudoOutputStream out = new SshSudoOutputStream(this, connection.getTempFile(getName()));
			out.open();
			if (logger.isDebugEnabled())
				logger.debug("Opened SUDO output stream to remote file " + this);
			return out;
		}
	}

	@Override
	public void mkdir() throws RuntimeIOException {
		if (!isTempFile) {
			super.mkdir();
		} else {
			/*
			 * For SUDO access, temporary dirs also need to be writable to the connecting user, otherwise an SCP copy will fail. 1777 is world writable with the
			 * sticky bit set.
			 */
			if (logger.isDebugEnabled()) {
				logger.debug("Creating world-writable directory (with sticky bit, mode 01777)");
			}
			mkdir("-m", "1777");
		}
	}

	@Override
	public void mkdirs() throws RuntimeIOException {
		if (!isTempFile) {
			super.mkdirs();
		} else {
			/*
			 * For SUDO access, temporary dirs also need to be writable to the connecting user, otherwise an SCP copy will fail. 1777 is world writable with the
			 * sticky bit set.
			 */
			if (logger.isDebugEnabled()) {
				logger.debug("Creating world-writable directories (with sticky bit, mode 01777)");
			}
			mkdir("-p", "-m", "1777");
		}
	}

	private Logger logger = LoggerFactory.getLogger(SshSudoFile.class);

}
