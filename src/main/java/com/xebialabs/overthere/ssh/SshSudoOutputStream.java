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

import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.LoggingOverthereProcessOutputHandler.loggingHandler;
import static com.xebialabs.overthere.util.MultipleOverthereProcessOutputHandler.multiHandler;

import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

/**
 * An output stream to a file on a host connected through SSH w/ SUDO.
 */
class SshSudoOutputStream extends OutputStream {

	private SshSudoOverthereFile destFile;

	private long length;

	private OverthereFile tempFile;

	private OutputStream tempFileOutputStream;

	public SshSudoOutputStream(SshSudoOverthereFile destFile, long length, OverthereFile tempFile) {
		this.destFile = destFile;
		this.length = length;
		this.tempFile = tempFile;
	}

	void open() {
		tempFileOutputStream = tempFile.getOutputStream(length);
	}

	@Override
	public void write(int b) throws IOException {
		tempFileOutputStream.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		tempFileOutputStream.write(b, off, len);
	}

	@Override
	public void write(byte[] b) throws IOException {
		tempFileOutputStream.write(b);
	}

	@Override
	public void close() throws IOException {
		tempFileOutputStream.close();
		copyTempFileToHostFile();
	}

	private void copyTempFileToHostFile() {
		if (logger.isDebugEnabled()) {
			logger.debug("Copying " + tempFile + " to " + destFile + " after writing");
		}

		CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
		int result = destFile.getConnection().execute(multiHandler(loggingHandler(logger), capturedOutput), CmdLine.build("cp", tempFile.getPath(), destFile.getPath()));
		if (result != 0) {
			String errorMessage = capturedOutput.getAll();
			throw new RuntimeIOException("Cannot copy " + tempFile + " to " + destFile + " after writing: " + errorMessage);
		}

		logger.info("Copied " + tempFile + " to " + destFile + " after writing");
	}

	private Logger logger = LoggerFactory.getLogger(SshSudoOutputStream.class);

}
