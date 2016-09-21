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
package com.xebialabs.overthere.cifs.telnet;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.CifsProcessConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;

/**
 * A connection to a Windows host using CIFS and Telnet.
 * <p/>
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator
 * access is needed.</li>
 * <li>Windows Telnet Service must be configured to use stream mode:<br/>
 * <tt>&gt; tlntadmn config mode=stream</tt></li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public class CifsTelnetConnection extends CifsProcessConnection {

    /**
     * Creates a {@link CifsTelnetConnection}. Don't invoke directly. Use
     * {@link Overthere#getConnection(String, ConnectionOptions)} instead.
     */
    public CifsTelnetConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper);
    }

    @Override
    public void connect() {
        super.connect();
    }

    @Override
    public OverthereProcess startProcess(final CmdLine cmd) {
        return super.startProcess(cmd);
    }
}
