/**
 * Copyright (c) 2008-2015, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.UnixCloudHostListener;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.UnixCloudHostListener.*;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;
import static com.xebialabs.overthere.ssh.SshConnectionType.INTERACTIVE_SUDO;

@Test
@Listeners({UnixCloudHostListener.class})
public class SshInteractiveSudoConnectionItest extends OverthereConnectionItestBase {


    @Override
    protected String getProtocol() {
        return SSH_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, UNIX);
        options.set(CONNECTION_TYPE, INTERACTIVE_SUDO);
        options.set(ADDRESS, UnixCloudHostListener.getHost().getHostName());
        options.set(PORT, 22);
        options.set(USERNAME, UNTRUSTED_UNIX_USER_USERNAME);
        options.set(PASSWORD, UNTRUSTED_UNIX_USER_PASSWORD);
        options.set(SUDO_USERNAME, REGULAR_UNIX_USER_USERNAME);
        options.set(SUDO_PASSWORD_PROMPT_REGEX, ".*[P|p]assword.*:");
        options.set(ALLOCATE_DEFAULT_PTY, true);
        options.set(SUDO_OVERRIDE_UMASK, true);
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return SshInteractiveSudoConnection.class.getName();
    }

}
