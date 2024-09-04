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
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.cifs.CifsProcessConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_WINDOWS;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;

/**
 * A connection to a Windows host using CIFS and the Windows native implementation of WinRM, i.e. the <tt>winrs</tt> command.
 */
public class CifsWinrsConnection  extends CifsProcessConnection {
    private static ConnectionOptions fixOptions(final ConnectionOptions options) {
        CifsConnectionType type = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);
        if (type.equals(CifsConnectionType.WINRM_NATIVE)) {
            ConnectionOptions fixedOptions = new ConnectionOptions(options);
            fixedOptions.set(FILE_COPY_COMMAND_FOR_WINDOWS, "copy {0} {1}");
            return fixedOptions;
        }
        return options;
    }

    public CifsWinrsConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, fixOptions(options), mapper);
    }

    @Override
    public void connect() {
        super.connect();
    }

    @Override
    public void doClose() {
        super.doClose();
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        return super.startProcess(cmd);
    }
}
