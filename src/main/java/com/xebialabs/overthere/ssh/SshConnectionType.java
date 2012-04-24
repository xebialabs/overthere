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

/**
 * Enumeration of SSH connection types.
 */
public enum SshConnectionType {

	/**
	 * An SSH connection that uses SFTP to transfer files, to a Unix host.
	 */
	SFTP,

	/**
	 * An SSH connection that uses SFTP to transfer files, to a Windows host running OpenSSH on Cygwin.
	 */
	SFTP_CYGWIN,

	/**
	 * An SSH connection that uses SFTP to transfer files, to a Windows host running WinSSHD.
	 */
	SFTP_WINSSHD,

	/**
	 * An SSH connection that uses SCP to transfer files, to a Unix host.
	 */
	SCP,

	/**
	 * An SSH connection that uses SCP to transfer files, to a Unix host. Uses SUDO, configured with NOPASSWD for all commands, to execute commands.
	 */
	SUDO,

	/**
	 * An SSH connection that uses SCP to transfer files, to a Unix host. Uses SUDO, <em>not</em> been configured with NOPASSWD for all commands, to execute commands.
	 */
	INTERACTIVE_SUDO,

	/**
	 * An SSH connection that is used for tunneling another connection through a 'jump station'. No operation on this actual connection can be performed.
	 */
	TUNNEL
}

