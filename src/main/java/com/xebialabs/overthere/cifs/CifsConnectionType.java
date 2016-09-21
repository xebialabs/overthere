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
package com.xebialabs.overthere.cifs;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.telnet.TelnetConnection;
import com.xebialabs.overthere.winrm.WinRmConnection;
import com.xebialabs.overthere.winrs.WinrsConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.ProcessConnection;

import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PORT_DEFAULT_WINRM_HTTPS;

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
    WINRM_NATIVE;

    public int getDefaultPort(ConnectionOptions options) {
        switch (this) {
            case TELNET:
                return PORT_DEFAULT_TELNET;
            case WINRM_INTERNAL:
            case WINRM_NATIVE:
                if (!options.getBoolean(WINRM_ENABLE_HTTPS, WINRM_ENABLE_HTTPS_DEFAULT)) {
                    return PORT_DEFAULT_WINRM_HTTP;
                } else {
                    return PORT_DEFAULT_WINRM_HTTPS;
                }
            default:
                throw new IllegalArgumentException("Unknown CIFS connection type " + this);
        }
    }

    public ProcessConnection getProcessConnection(ConnectionOptions options,
                                                  AddressPortMapper mapper, OverthereFile workingDirectory) {
        ProcessConnection connection;
        switch (this) {
            case TELNET:
                connection = new TelnetConnection(options, mapper, workingDirectory);
                break;
            case WINRM_INTERNAL:
                connection = new WinRmConnection(options, mapper, workingDirectory);
                break;
            case WINRM_NATIVE:
                connection = new WinrsConnection(options, mapper, workingDirectory);
                break;
            default:
                throw new IllegalArgumentException("Unknown CIFS connection type " + this);
        }
        return connection;
    }

}
