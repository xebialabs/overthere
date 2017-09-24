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

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.cifs.CifsConnectionType;
import com.xebialabs.overthere.spi.ProcessConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.CONNECTION_TYPE;

public class SmbProcessConnection extends SmbConnection {

    private ProcessConnection processConnection;

    public SmbProcessConnection(String type, ConnectionOptions options,
                                AddressPortMapper mapper) {
        super(type, options, mapper, true);
        options.set(ConnectionOptions.PROTOCOL, type);
        CifsConnectionType cifsConnectionType = options.getEnum(CONNECTION_TYPE, CifsConnectionType.class);
        processConnection = cifsConnectionType.getProcessConnection(options, mapper, workingDirectory);
    }

    @Override
    public void connect() {
        processConnection.connect();
        super.connect();
        connected();
    }

    @Override
    public void setWorkingDirectory(OverthereFile workingDirectory) {
        super.setWorkingDirectory(workingDirectory);
        processConnection.setWorkingDirectory(workingDirectory);
    }

    @Override
    public void doClose() {
        super.doClose();
        processConnection.close();
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        return processConnection.startProcess(cmd);
    }
}
