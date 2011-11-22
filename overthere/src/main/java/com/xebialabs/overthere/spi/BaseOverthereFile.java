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
package com.xebialabs.overthere.spi;

import static com.google.common.base.Preconditions.checkArgument;

import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.OverthereFileCopier;

/**
 * A file system object (file, directory, etc.) on a remote system that is accessible through an {@link OverthereConnection}.
 */
public abstract class BaseOverthereFile<C extends OverthereConnection> implements OverthereFile {

	protected C connection;

	protected BaseOverthereFile() {
		this.connection = null;
	}

	protected BaseOverthereFile(C connection) {
		this.connection = connection;
	}

	@Override
	public C getConnection() {
		return connection;
	}

	@Override
	public OverthereFile getFile(String child) {
		return getConnection().getFile(this, child);
	}

	@Override
	public void deleteRecursively() throws RuntimeIOException {
		if (isDirectory()) {
			for (OverthereFile each : listFiles()) {
				each.deleteRecursively();
			}
		}

		delete();
	}

	@Override
	public final void copyTo(final OverthereFile dest) {
		checkArgument(dest instanceof BaseOverthereFile<?>, "dest is not a subclass of BaseOverthereFile");

		((BaseOverthereFile<?>) dest).copyFrom(this);
	}

	protected void copyFrom(OverthereFile source) {
		OverthereFileCopier.copy(source, this);
	}

	/**
	 * Subclasses MUST implement toString properly.
	 */
	@Override
	public abstract String toString();

}
