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
package com.xebialabs.overthere.smb.winrm;

import com.xebialabs.overthere.smb.SmbProcessConnection;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.xebialabs.overthere.ConnectionOptions;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.*;
import static com.xebialabs.overthere.util.DefaultAddressPortMapper.INSTANCE;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;

public class SmbWinRmConnectionTest {

    private ConnectionOptions options;

    @BeforeMethod
    public void setupOptions() {
        options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM_INTERNAL);
        options.set(PASSWORD, "foobar");
        options.set(SMB_PORT, PORT_DEFAULT_SMB);
        options.set(PORT, PORT_DEFAULT_WINRM_HTTP);
        options.set(ADDRESS, "localhost");
    }

    @Test
    @SuppressWarnings("resource")
    public void shouldSupportNewStyleDomainAccount() {
        options.set(USERNAME, "user@domain.com");
        new SmbProcessConnection(SMB_PROTOCOL, options, INSTANCE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("resource")
    public void shouldNotSupportOldStyleDomainAccount() {
        options.set(USERNAME, "domain\\user");
        new SmbProcessConnection(SMB_PROTOCOL, options, INSTANCE);
    }

    @Test
    @SuppressWarnings("resource")
    public void shouldSupportDomainlessAccount() {
        options.set(USERNAME, "user");
        new SmbProcessConnection(SMB_PROTOCOL, options, INSTANCE);
    }

}
