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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;

import static com.xebialabs.overthere.ConnectionOptions.*;

import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_NATIVE;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.*;
import static com.xebialabs.overthere.util.DefaultAddressPortMapper.INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class SmbFileTest {

    private ConnectionOptions options;

    @BeforeMethod
    public void setupOptions() {
        options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM_NATIVE);
        options.set(PASSWORD, "foobar");
        options.set(PORT, PORT_DEFAULT_WINRM_HTTP);
        options.set(SMB_PORT, PORT_DEFAULT_SMB);
        options.set(ADDRESS, "localhost");
    }


    @Test
    public void shouldReturnNullForParentFileOfRoot() {
        options.set(USERNAME, "user@domain.com");
        SmbProcessConnection smbWinRmConnection = new SmbProcessConnection(SMB_PROTOCOL, options, INSTANCE);
        OverthereFile file = smbWinRmConnection.getFile("C:\\");
        assertThat(file.getParentFile(), nullValue());
    }

    @Test
    public void shouldReturnParentFileForFile() {
        options.set(USERNAME, "user@domain.com");
        SmbProcessConnection smbWinRmConnection = new SmbProcessConnection(SMB_PROTOCOL, options, INSTANCE);
        OverthereFile file = smbWinRmConnection.getFile("C:\\windows\\temp\\ot-2015060");
        assertThat((file = file.getParentFile()).getPath(), equalTo("C:\\windows\\temp"));
        assertThat((file = file.getParentFile()).getPath(), equalTo("C:\\windows"));
        assertThat((file = file.getParentFile()).getPath(), equalTo("C:"));
        assertThat(file.getParentFile(), nullValue());
    }

    @Test
    public void shouldSucceedForNonRoot() {
        options.set(USERNAME, "user@domain.com");
        SmbProcessConnection smbWinRmConnection = new SmbProcessConnection(SMB_PROTOCOL, options, INSTANCE);
        OverthereFile file = smbWinRmConnection.getFile("C:\\windows\\temp\\ot-2015060");
        assertThat(file.getParentFile(), not(nullValue()));
    }

    @Test
    public void shouldConvertForwardSlashInFilePathToBackSlash() {
        options.set(USERNAME, "user@domain.com");
        SmbProcessConnection smbWinRmConnection = new SmbProcessConnection(SMB_PROTOCOL, options, INSTANCE);
        OverthereFile file = smbWinRmConnection.getFile("C:\\windows/temp/ot-2015060");
        assertThat(file.getPath(), equalTo("C:\\windows\\temp\\ot-2015060"));
    }
}