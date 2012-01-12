/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
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

package com.xebialabs.overthere.spi;

import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.util.OverthereFileCopier;

import static com.google.common.base.Preconditions.checkArgument;

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

