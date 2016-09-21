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
package com.xebialabs.overthere.smb.telnet;

import com.xebialabs.overthere.smb.SmbProcessConnection;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.WindowsCloudHostListener;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.WindowsCloudHostListener.ADMINISTRATIVE_WINDOWS_USER_PASSWORD;
import static com.xebialabs.overthere.WindowsCloudHostListener.ADMINISTRATIVE_WINDOWS_USER_USERNAME;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.SMB_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;

@Test
@Listeners({WindowsCloudHostListener.class})
public class SmbTelnetConnectionWithAdministrativeUserItest extends OverthereConnectionItestBase {

    @Override
    protected String getProtocol() {
        return SMB_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(ADDRESS, WindowsCloudHostListener.getHost().getHostName());
        options.set(CONNECTION_TYPE, TELNET);
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(USERNAME, ADMINISTRATIVE_WINDOWS_USER_USERNAME);
        options.set(PASSWORD, ADMINISTRATIVE_WINDOWS_USER_PASSWORD);
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return SmbProcessConnection.class.getName();
    }

}
