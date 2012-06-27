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

package com.xebialabs.overthere.cifs;

/**
 * Enumeration of CIFS connection types.
 */
public enum CifsConnectionType {
	
	/**
	 * A CIFS connection that uses Telnet to execute commands, to a Windows host.
	 */
	TELNET,
	
	/**
	 * A CIFS connection that uses WinRM over HTTP to execute commands, to a Windows host.
	 */
	WINRM_HTTP,
	
	/**
	 * A CIFS connection that uses WinRM over HTTPS to execute commands, to a Windows host.
	 */
	WINRM_HTTPS,

	/**
	 * A CIFS Connection that uses WinRM over HTTP with Kerberos Authentication to execute commands, to a windows
	 * host.
	 */
	WINRM_HTTP_KB5,

	/**
	 * A CIFS Connection that uses WinRM over HTTPS with Kerberos Authentication to execute commands, to a windows host.
	 */
	WINRM_HTTPS_KB5,

}

