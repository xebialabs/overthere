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
package com.xebialabs.overthere.smb;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.cifs.ConnectionBuilder;
import com.xebialabs.overthere.smb.telnet.SmbTelnetConnection;
import com.xebialabs.overthere.smb.winrm.SmbWinRmConnection;
import com.xebialabs.overthere.smb.winrs.SmbWinrsConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

import static com.xebialabs.overthere.smb.SmbConnectionBuilder.SMB_PROTOCOL;

@Protocol(name = SMB_PROTOCOL)
public class SmbConnectionBuilder extends ConnectionBuilder implements OverthereConnectionBuilder {

    public static final String SMB_PROTOCOL = "smb";

    /**
     * The default port for SMB connections over TCP/IP
     */
    public static final int PORT_DEFAULT_SMB = 445;

    public static final String SMB_PORT = "smbPort";

    private final SmbConnection connection;

    public SmbConnectionBuilder(String type, ConnectionOptions options, AddressPortMapper mapper) {
        CifsConnectionType cifsConnectionType = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);

        switch (cifsConnectionType) {
            case TELNET:
                connection = new SmbTelnetConnection(type, options, mapper);
                break;
            case WINRM_INTERNAL:
                connection = new SmbWinRmConnection(type, options, mapper);
                break;
            case WINRM_NATIVE:
                connection = new SmbWinrsConnection(type, options, mapper);
                break;
            default:
                throw new IllegalArgumentException("Unknown CIFS connection type " + cifsConnectionType);
        }
    }

    @Override
    public OverthereConnection connect() {
        connection.connect();
        return connection;
    }

    @Override
    public String toString() {
        return connection.toString();
    }
}
