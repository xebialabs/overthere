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

import java.io.InputStream;
import java.io.OutputStream;

import com.xebialabs.overthere.RuntimeIOException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;

import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.CommandExecutionCallbackHandler;
import com.xebialabs.overthere.OverthereFile;
import org.slf4j.LoggerFactory;

/**
 * A file on a host connected through SSH w/ SUDO.
 */
@SuppressWarnings("serial")
class SshSudoOverthereFile extends SshScpOverthereFile {

	private SshSudoHostConnection sshSudoHostSession;

	private boolean isTempFile;

	/**
	 * Constructs a SshSudoHostFile
	 * 
	 * @param session
	 *            the connection connected to the host
	 * @param remotePath
	 *            the path of the file on the host
	 * @param isTempFile
	 *            is <code>true</code> if this is a temporary file; <code>false</code> otherwise
	 */
	public SshSudoOverthereFile(SshSudoHostConnection session, String remotePath, boolean isTempFile) {
		super(session, remotePath);
		this.sshSudoHostSession = session;
		this.isTempFile = isTempFile;
	}

	@Override
	protected int executeCommand(CommandExecutionCallbackHandler handler, String... command) {
		if (isTempFile) {
			return sshSudoHostSession.noSudoExecute(handler, command);
		} else {
			return super.executeCommand(handler, command);
		}
	}

	@Override
	public OverthereFile getFile(String name) {
		SshSudoOverthereFile f = (SshSudoOverthereFile) super.getFile(name);
		f.isTempFile = this.isTempFile;
		return f;
	}

	@Override
	public OverthereFile getParentFile() {
		SshSudoOverthereFile f = (SshSudoOverthereFile) super.getParentFile();
		f.isTempFile = this.isTempFile;
		return f;
	}

	@Override
	public InputStream get() throws RuntimeIOException {
		if (isTempFile) {
			return super.get();
		} else {
			OverthereFile tempFile = getTempFile(true);
			copyHostFileToTempFile(tempFile);
			return tempFile.get();
		}
	}

	private void copyHostFileToTempFile(OverthereFile tempFile) {
		if (logger.isDebugEnabled())
			logger.debug("Copying " + this + " to " + tempFile + " for reading");
		CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
		int result = connection.execute(capturedOutput, "cp", this.getPath(), tempFile.getPath());
		if (result != 0) {
			String errorMessage = capturedOutput.getAll();
			throw new RuntimeIOException("Cannot copy " + this + " to " + tempFile + " for reading: " + errorMessage);
		}
	}

	@Override
	public OutputStream put(long length) throws RuntimeIOException {
		if (isTempFile) {
			// XXX: this should really be returning a "ChmoddingSudoOutputStream"
			return super.put(length);
		} else {
			/*
			 * XXX: this should really be returning a "ChmoddingSudoOutputStream" wrapped in a "CopyOnCompletionOutputStream"
			 */
			SshSudoOutputStream out = new SshSudoOutputStream(this, length, getTempFile(false));
			out.open();
			if (logger.isDebugEnabled())
				logger.debug("Opened SUDO output stream to remote file " + this);
			return out;
		}
	}

	protected OverthereFile getTempFile(boolean useSudoForDeletion) {
		String prefix = FilenameUtils.getBaseName(getPath());
		String suffix = FilenameUtils.getExtension(getPath());
		return sshSudoHostSession.getTempFile(prefix, suffix);
	}

	@Override
	public boolean mkdir() throws RuntimeIOException {
		if (!isTempFile) {
			return super.mkdir();
		} else {
			/*
			 * For SUDO access, temporary dirs also need to be writable to the connecting user, otherwise an SCP copy will fail. 1777 is world writable with the
			 * sticky bit set.
			 */
			logger.debug("Making directory world-writable (with sticky bit)");
			return mkdir(new String[] { "-m", "1777" });
		}
	}

	private Logger logger = LoggerFactory.getLogger(SshSudoOverthereFile.class);

}

