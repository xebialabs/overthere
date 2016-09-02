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
package com.xebialabs.overthere.smb2.telnet;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.ConnectionValidator;
import com.xebialabs.overthere.cifs.telnet.TelnetConnection;
import com.xebialabs.overthere.smb2.Smb2Connection;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.xebialabs.overthere.smb2.Smb2ConnectionBuilder.SMB2_PROTOCOL;


/**
 * A connection to a Windows host using SMB2 and Telnet.
 * <p/>
 * Limitations:
 * <ul>
 * <li>Windows Telnet Service must be configured to use stream mode:<br/>
 * <tt>&gt; tlntadmn config mode=stream</tt></li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public class Smb2TelnetConnection extends Smb2Connection {

    /**
     * Creates a {@link Smb2TelnetConnection}. Don't invoke directly. Use
     * {@link Overthere#getConnection(String, ConnectionOptions)} instead.
     */
    public Smb2TelnetConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper, true);
        ConnectionValidator.assertIsWindowsHost(os, SMB2_PROTOCOL, cifsConnectionType);
        ConnectionValidator.assertNotNewStyleWindowsDomain(username, SMB2_PROTOCOL, cifsConnectionType);
        connected();
    }

    @Override
    public void connect() {
        super.connect();
        connected();
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        TelnetConnection connection = new TelnetConnection(options, mapper, workingDirectory);
        return connection.startProcess(cmd);
    }
}
