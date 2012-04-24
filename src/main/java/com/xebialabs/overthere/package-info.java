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

/**
 * Contains the core API of the Overthere library. This library allows users to work with local or remote systems through an abstraction layer. Some of the features:
 * <ul>
 * <li>Files can be manipulated, read and written.<li>
 * <li>Commands can be executed.</li>
 * <li>A local system ("localhost") can be accessed directly.</li>
 * <li>A remote system can be accessed using any number of connection methods implemented: SSH, CIFS/Telnet, etc.</li>
 * <li>The host can be a Windows or a Unix machine.</li>
 * </ul>
 * 
 * To start, create a {@link com.xebialabs.overthere.OverthereConnection connection} by invoking {@link com.xebialabs.overthere.Overthere#getConnection(String, ConnectionOptions)}.
 * Then, create file objects using {@link com.xebialabs.overthere.OverthereConnection#getFile(String)} and manipulate them or execute commands using
 * {@link com.xebialabs.overthere.OverthereConnection#execute(OverthereProcessOutputHandler, CmdLine)}).
 *
 * @version 1.0
 */
package com.xebialabs.overthere;


