/*
 * Copyright (c) 2008-2014, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere;

import java.util.concurrent.atomic.AtomicReference;

import com.xebialabs.overcast.CloudHost;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.TUNNEL;

public class WindowsCloudHostListener extends CloudHostListener {

    private static AtomicReference<CloudHost> windowsHost = new AtomicReference<CloudHost>();

    public WindowsCloudHostListener() {
        super("overthere-windows", windowsHost);
    }

    public static CloudHost getHost() {
        return checkNotNull(windowsHost.get(), "Cloud host overthere-windows has not been started. Did you wire up the right CloudHostListener?");
    }

    public static ConnectionOptions getOptionsForTunnel() {
        ConnectionOptions tunnelOptions = new ConnectionOptions();
        tunnelOptions.set(OPERATING_SYSTEM, WINDOWS);
        tunnelOptions.set(CONNECTION_TYPE, TUNNEL);
        tunnelOptions.set(ADDRESS, getHost().getHostName());
        tunnelOptions.set(PORT, 22);
        tunnelOptions.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
        tunnelOptions.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
        return tunnelOptions;
    }

    public static final String ADMINISTRATIVE_USER_ITEST_USERNAME = "Administrator";
    public static final String ADMINISTRATIVE_USER_ITEST_PASSWORD = "iW8tcaM0d";

    public static final String REGULAR_USER_ITEST_USERNAME = "overthere";
    public static final String REGULAR_USER_ITEST_PASSWORD = "wLitdMy@:;<>KY9";
    // The password for the regular user includes special characters to test that they get encoded correctly

}
