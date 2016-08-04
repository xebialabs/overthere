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
package com.xebialabs.overthere.cifs.winrs;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.CifsConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*;
import static com.xebialabs.overthere.util.OverthereUtils.*;
import static java.lang.String.format;

/**
 * A connection to a Windows host using CIFS and the Windows native implementation of WinRM, i.e. the <tt>winrs</tt> command.
 */
public class CifsWinrsConnection extends CifsConnection {

    private WinrsConnection connection;

    private ConnectionOptions options;

    public CifsWinrsConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper, true);
        checkArgument(os == WINDOWS, "Cannot create a " + CIFS_PROTOCOL + ":%s connection to a machine that is not running Windows", cifsConnectionType.toString().toLowerCase());
        checkArgument(mapper instanceof DefaultAddressPortMapper, "Cannot create a " + CIFS_PROTOCOL + ":%s connection when connecting through a SSH jumpstation", cifsConnectionType.toString().toLowerCase());
        checkArgument(password.indexOf('\'') == -1 && password.indexOf('\"') == -1, "Cannot create a " + CIFS_PROTOCOL + ":%s connection with a password that contains a single quote (\') or a double quote (\")", cifsConnectionType.toString().toLowerCase());

        this.options = options;
    }

    @Override
    public void connect() {
        connection = new WinrsConnection(options, mapper, workingDirectory);
        connection.connectToWinrsProxy(options);

        if (connection.getWinrsProxyConnection().getHostOperatingSystem() != WINDOWS) {
            connection.disconnectFromWinrsProxy();
            throw new IllegalArgumentException(format("Cannot create a " + CIFS_PROTOCOL + ":%s connection with a winrs proxy that is not running Windows", cifsConnectionType.toString().toLowerCase()));
        }

        connected();
    }

    @Override
    public void doClose() {
        connection.disconnectFromWinrsProxy();
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        return connection.startProcess(cmd);
    }
}
