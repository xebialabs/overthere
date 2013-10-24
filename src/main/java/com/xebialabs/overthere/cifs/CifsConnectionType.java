/*
 * Copyright (c) 2008-2013, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.cifs;

/**
 * Enumeration of CIFS connection types.
 */
public enum CifsConnectionType {

    /**
     * A CIFS connection to a Windows host that uses Telnet to execute commands.
     */
    TELNET,

    /**
     * A CIFS connection to a Windows host that uses a Java implementation WinRM to execute commands.
     */
    WINRM_INTERNAL,
    
    /**
     * A CIFS connection  to a Windows host that uses the <code>winrs</code> command native to Windows to execute commands.
     * <em>N.B.:</em> This implementation only works when Overthere runs on Windows.
     */
    WINRM_NATIVE

}
