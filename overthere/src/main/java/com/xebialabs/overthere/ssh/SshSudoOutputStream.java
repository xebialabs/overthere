/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.OverthereFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream to a file on a host connected through SSH w/ SUDO.
 */
class SshSudoOutputStream extends OutputStream {

	private SshSudoFile destFile;

	private OverthereFile tempFile;

	private OutputStream tempFileOutputStream;

	public SshSudoOutputStream(SshSudoFile destFile, OverthereFile tempFile) {
		this.destFile = destFile;
		this.tempFile = tempFile;
		tempFileOutputStream = tempFile.getOutputStream();
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
		destFile.copyfromTempFile(tempFile);
	}

}

