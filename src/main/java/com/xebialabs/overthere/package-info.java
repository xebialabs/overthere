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
/**
 * The host connection framework. This framework allows plugin authors to work with local or remote systems ("hosts") conveniently. Some of the features:
 * <ul>
 * <li>Files can be manipulated, read and written.<li>
 * <li>Commands can be executed.</li>
 * <li>A remote system can be accessed using SSH in one of three methods: SFTP, SCP or SCP with SUDO.</li>
 * <li>A local system ("localhost") can be accessed directly.</li>
 * <li>The host can be a Windows or a Unix machine.</li>
 * </ul>
 *
 * @version 1.2
 */
package com.xebialabs.overthere;


