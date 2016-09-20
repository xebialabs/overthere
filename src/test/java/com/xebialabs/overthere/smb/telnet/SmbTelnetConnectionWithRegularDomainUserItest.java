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

import com.google.common.collect.ImmutableMap;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.WindowsCloudHostWithDomainListener;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import com.xebialabs.overthere.smb.SmbProcessConnection;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.WindowsCloudHostWithDomainListener.DOMAIN_WINDOWS_USER_PASSWORD;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.PATH_SHARE_MAPPINGS;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.SMB_PROTOCOL;

@Test
@Listeners({WindowsCloudHostWithDomainListener.class})
public class SmbTelnetConnectionWithRegularDomainUserItest extends OverthereConnectionItestBase {

    public static final String WINDOWS_USERNAME = "W2K8R2\\itest";

    @Override
    protected String getProtocol() {
        return SMB_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, TELNET);
        options.set(ADDRESS, WindowsCloudHostWithDomainListener.getHost().getHostName());
        options.set(USERNAME, WINDOWS_USERNAME);
        options.set(PASSWORD, DOMAIN_WINDOWS_USER_PASSWORD);
        options.set(TEMPORARY_DIRECTORY_PATH, "C:\\overthere\\temp");
        options.set(PATH_SHARE_MAPPINGS, ImmutableMap.of("C:\\overthere", "sharethere"));
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return SmbProcessConnection.class.getName();
    }

}
