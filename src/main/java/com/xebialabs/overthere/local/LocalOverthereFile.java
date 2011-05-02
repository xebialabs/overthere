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
package com.xebialabs.overthere.local;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.xebialabs.overthere.HostConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * A local file.
 */
@SuppressWarnings("serial")
class LocalOverthereFile extends OverthereFile {

	public LocalOverthereFile(HostConnection connection, String path) {
		super(connection, path);
	}

	@Override
	public InputStream get() {
		try {
			return new FileInputStream(this);
		} catch (FileNotFoundException exc) {
			throw new RuntimeIOException("Cannot open " + this + " for reading", exc);
		}
	}

	@Override
	public OutputStream put(long length) {
		try {
			return new FileOutputStream(this);
		} catch (FileNotFoundException exc) {
			throw new RuntimeIOException("Cannot open " + this + " for writing", exc);
		}
	}

}
