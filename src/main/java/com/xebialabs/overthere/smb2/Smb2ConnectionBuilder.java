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
package com.xebialabs.overthere.smb2;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.cifs.ConnectionBuilder;
import com.xebialabs.overthere.smb2.telnet.Smb2TelnetConnection;
import com.xebialabs.overthere.smb2.winrm.Smb2WinRmConnection;
import com.xebialabs.overthere.smb2.winrs.Smb2WinrsConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

import static com.xebialabs.overthere.smb2.Smb2ConnectionBuilder.SMB2_PROTOCOL;

@Protocol(name = SMB2_PROTOCOL)
public class Smb2ConnectionBuilder extends ConnectionBuilder implements OverthereConnectionBuilder {

    public static final String SMB2_PROTOCOL = "smb2";

    /**
     * The default port for SMB2 connections over TCP/IP
     */
    public static final int PORT_DEFAULT_SMB2 = 445;

    public static final String SMB2_PORT = "cifsPort";

    /**
     * The Windows Domain to authenticate the user against. If not set, bla bla bla
     */
    public static final String DOMAIN = "domain";

    private final Smb2Connection connection;

    public Smb2ConnectionBuilder(String type, ConnectionOptions options, AddressPortMapper mapper) {
        CifsConnectionType cifsConnectionType = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);

        switch (cifsConnectionType) {
            case TELNET:
                connection = new Smb2TelnetConnection(type, options, mapper);
                break;
            case WINRM_INTERNAL:
                connection = new Smb2WinRmConnection(type, options, mapper);
                break;
            case WINRM_NATIVE:
                connection = new Smb2WinrsConnection(type, options, mapper);
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
