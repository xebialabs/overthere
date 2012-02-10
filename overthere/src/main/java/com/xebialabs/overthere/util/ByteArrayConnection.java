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

package com.xebialabs.overthere.util;

import com.xebialabs.overthere.BaseOverthereConnection;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;

class ByteArrayConnection extends BaseOverthereConnection {

	protected ByteArrayConnection(String protocol, ConnectionOptions options) {
	    super(protocol, options, false);
    }

	@Override
	protected void doClose() {
		// no-op
	}

	@Override
	public OverthereFile getFile(String hostPath) {
		throw new UnsupportedOperationException("ByteArrayConnection has no functionality. Use only the created ByteArrayFile.");
	}

	@Override
	public OverthereFile getFile(OverthereFile parent, String child) {
		throw new UnsupportedOperationException("ByteArrayConnection has no functionality. Use only the created ByteArrayFile.");
	}

	@Override
	protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
		throw new UnsupportedOperationException("ByteArrayConnection has no functionality. Use only the created ByteArrayFile.");
	}

	@Override
	public String toString() {
		return "byte_array://";
	}

}

