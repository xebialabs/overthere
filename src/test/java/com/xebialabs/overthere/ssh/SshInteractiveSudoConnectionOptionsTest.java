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
package com.xebialabs.overthere.ssh;

import java.lang.reflect.Field;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_DEFAULT_PTY;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_PTY;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class SshInteractiveSudoConnectionOptionsTest {

    private ConnectionOptions connectionOptions;

    @BeforeClass
    public void init() {
        connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SFTP);
        connectionOptions.set(OPERATING_SYSTEM, UNIX);
        connectionOptions.set(ADDRESS, "nowhere.example.com");
        connectionOptions.set(USERNAME, "some-user");
        connectionOptions.set(PASSWORD, "foo");
        connectionOptions.set(SUDO_USERNAME, "some-other-user");
        connectionOptions.set(ALLOCATE_DEFAULT_PTY, true);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("resource")
    public void shouldNotAcceptPasswordPromptRegexWithWildcardStar() {
        ConnectionOptions options = new ConnectionOptions(connectionOptions);
        options.set(SUDO_PASSWORD_PROMPT_REGEX, "assword*");
        new SshInteractiveSudoConnection(SSH_PROTOCOL, options, new DefaultAddressPortMapper());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("resource")
    public void shouldNotAcceptPasswordPromptRegexWithWildcardQuestion() {
        ConnectionOptions options = new ConnectionOptions(connectionOptions);
        options.set(SUDO_PASSWORD_PROMPT_REGEX, "assword?");
        new SshInteractiveSudoConnection(SSH_PROTOCOL, options, new DefaultAddressPortMapper());
    }

    @Test
    @SuppressWarnings("resource")
    public void shouldAcceptPasswordPromptRegex() {
        ConnectionOptions options = new ConnectionOptions(connectionOptions);
        options.set(SUDO_PASSWORD_PROMPT_REGEX, "[Pp]assword.*:");
        SshInteractiveSudoConnection connection = new SshInteractiveSudoConnection(SSH_PROTOCOL, options, new DefaultAddressPortMapper());
        connection.close();
    }

    @Test
    public void shouldSetAllocatePtyIfNoDefaultPtyAndNoPtySet() throws NoSuchFieldException, IllegalAccessException {
        ConnectionOptions options = new ConnectionOptions(connectionOptions);
        options.set(ALLOCATE_DEFAULT_PTY, false);
        options.set(ALLOCATE_PTY, null);
        SshInteractiveSudoConnection sshInteractiveSudoConnection = new SshInteractiveSudoConnection(SSH_PROTOCOL, options, new DefaultAddressPortMapper());
        try {
            Field allocatePty = SshConnection.class.getDeclaredField("allocatePty");
            allocatePty.setAccessible(true);
            assertThat((String) allocatePty.get(sshInteractiveSudoConnection), equalTo("vt220:80:24:0:0"));
        } finally {
            closeQuietly(sshInteractiveSudoConnection);
        }

    }
}
