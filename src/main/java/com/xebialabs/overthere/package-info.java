/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
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

