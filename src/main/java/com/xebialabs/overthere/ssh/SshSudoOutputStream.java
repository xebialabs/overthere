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
