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
package com.xebialabs.overthere;

import java.util.concurrent.atomic.AtomicReference;

import com.xebialabs.overcast.host.CloudHost;

import static com.xebialabs.overthere.Utils.getProperty;
import static com.xebialabs.overthere.util.OverthereUtils.checkNotNull;

public class WindowsCloudHostListener extends CloudHostListener {

    public static final String REGULAR_WINDOWS_USER_USERNAME = "overthere-user";
    public static final String REGULAR_WINDOWS_USER_PASSWORD = getProperty("test.overthere.windows.regular.password");
    // The password for the regular user includes special characters to test that they get encoded correctly

    public static final String ADMINISTRATIVE_WINDOWS_USER_USERNAME = "Administrator";
    public static final String ADMINISTRATIVE_WINDOWS_USER_PASSWORD = getProperty("test.overthere.windows.administrative.password");;

    private static AtomicReference<CloudHost> windowsHost = new AtomicReference<CloudHost>();

    public WindowsCloudHostListener() {
        super("overthere-windows-local-accounts", windowsHost);
    }

    public static CloudHost getHost() {
        return checkNotNull(windowsHost.get(), "Cloud host overthere-windows-local has not been started. Did you wire up the right CloudHostListener?");
    }

}
