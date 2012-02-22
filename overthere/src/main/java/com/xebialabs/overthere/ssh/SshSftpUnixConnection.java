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

package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.spi.AddressPortResolver;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;

/**
 * A connection to a Unix host using SSH w/ SFTP.
 */
class SshSftpUnixConnection extends SshSftpConnection {

	public SshSftpUnixConnection(String type, ConnectionOptions options, AddressPortResolver resolver) {
	    super(type, options, resolver);
		checkArgument(os != WINDOWS, "Cannot start a " + SSH_PROTOCOL + ":%s connection to a Windows operating system", sshConnectionType.toString().toLowerCase());
    }

	@Override
	protected String pathToSftpPath(String path) {
	    return path;
    }

}

