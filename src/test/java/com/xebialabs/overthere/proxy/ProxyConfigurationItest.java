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
package com.xebialabs.overthere.proxy;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.UnixCloudHostListener;
import com.xebialabs.overthere.cifs.CifsConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.UnixCloudHostListener.REGULAR_UNIX_USER_PASSWORD;
import static com.xebialabs.overthere.UnixCloudHostListener.REGULAR_UNIX_USER_USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.proxy.ProxyConnection.PROXY_PROTOCOL;
import static com.xebialabs.overthere.proxy.ProxyConnection.PROXY_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;
import static com.xebialabs.overthere.ssh.SshJumpstationConnectionBuilder.SSH_JUMPSTATION_PROTOCOL;
import static java.net.Proxy.Type.HTTP;
import static java.net.Proxy.Type.SOCKS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.fail;

@Test
@Listeners({UnixCloudHostListener.class})
public class ProxyConfigurationItest {

    @Test
    public void shouldNotConnectThroughTwoProxies() {
        try {
            ConnectionOptions proxy2Options = new ConnectionOptions();
            proxy2Options.set(PROTOCOL, PROXY_PROTOCOL);
            proxy2Options.set(PROXY_TYPE, HTTP);
            proxy2Options.set(ADDRESS, "proxy2");
            proxy2Options.set(PORT, 1080);

            ConnectionOptions proxy1Options = new ConnectionOptions();
            proxy1Options.set(PROTOCOL, PROXY_PROTOCOL);
            proxy1Options.set(PROXY_TYPE, HTTP);
            proxy1Options.set(ADDRESS, "proxy1");
            proxy1Options.set(PORT, 1080);
            proxy1Options.set(JUMPSTATION, proxy2Options);

            ConnectionOptions options = new ConnectionOptions();
            options.set(SshConnectionBuilder.CONNECTION_TYPE, SCP);
            options.set(OPERATING_SYSTEM, UNIX);
            options.set(ADDRESS, "host");
            options.set(USERNAME, "username");
            options.set(PASSWORD, "password");
            options.set(JUMPSTATION, proxy1Options);

            Overthere.getConnection(SSH_PROTOCOL, options);

            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Cannot configure an HTTP proxy behind another proxy or behind an SSH jumpstation"));
        }
    }

    @Test(enabled = false)
    public void shouldNotConnectThroughProxyBehindJumpstation() {
        try {
            ConnectionOptions jumpstationOptions = new ConnectionOptions();
            jumpstationOptions.set(PROTOCOL, SSH_JUMPSTATION_PROTOCOL);
            jumpstationOptions.set(ADDRESS, UnixCloudHostListener.getHost().getHostName());
            jumpstationOptions.set(OPERATING_SYSTEM, UNIX);
            jumpstationOptions.set(PORT, 22);
            jumpstationOptions.set(USERNAME, REGULAR_UNIX_USER_USERNAME);
            jumpstationOptions.set(PASSWORD, REGULAR_UNIX_USER_PASSWORD);

            ConnectionOptions proxyOptions = new ConnectionOptions();
            proxyOptions.set(PROTOCOL, PROXY_PROTOCOL);
            proxyOptions.set(PROXY_TYPE, HTTP);
            proxyOptions.set(ADDRESS, "proxy2");
            proxyOptions.set(PORT, 1080);
            proxyOptions.set(JUMPSTATION, jumpstationOptions);

            ConnectionOptions options = new ConnectionOptions();
            options.set(SshConnectionBuilder.CONNECTION_TYPE, SCP);
            options.set(OPERATING_SYSTEM, UNIX);
            options.set(ADDRESS, "host");
            options.set(USERNAME, "username");
            options.set(PASSWORD, "password");
            options.set(JUMPSTATION, proxyOptions);

            Overthere.getConnection(SSH_PROTOCOL, options);

            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Cannot configure an HTTP proxy behind another proxy or behind an SSH jumpstation"));
        }
    }

    @Test
    public void shouldNotConnectOverSocksProxy() {
        try {
            ConnectionOptions proxyOptions = new ConnectionOptions();
            proxyOptions.set(PROTOCOL, "proxy");
            proxyOptions.set(PROXY_TYPE, SOCKS);
            proxyOptions.set(ADDRESS, "proxy");
            proxyOptions.set(PORT, 1080);

            ConnectionOptions options = new ConnectionOptions();
            options.set(SshConnectionBuilder.CONNECTION_TYPE, SCP);
            options.set(OPERATING_SYSTEM, UNIX);
            options.set(ADDRESS, "host");
            options.set(USERNAME, "username");
            options.set(PASSWORD, "password");
            options.set(JUMPSTATION, proxyOptions);

            Overthere.getConnection(SSH_PROTOCOL, options);

            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Proxy of type other than HTTP not supported"));
        }
    }

    @Test
    public void shouldNotConnectCifsOverProxy() {
        try {
            ConnectionOptions proxyOptions = new ConnectionOptions();
            proxyOptions.set(PROTOCOL, "proxy");
            proxyOptions.set(PROXY_TYPE, HTTP);
            proxyOptions.set(ADDRESS, "proxy");
            proxyOptions.set(PORT, 8888);

            ConnectionOptions options = new ConnectionOptions();
            options.set(CifsConnectionBuilder.CONNECTION_TYPE, WINRM_INTERNAL);
            options.set(OPERATING_SYSTEM, WINDOWS);
            options.set(ADDRESS, "host");
            options.set(USERNAME, "username");
            options.set(PASSWORD, "password");
            options.set(JUMPSTATION, proxyOptions);

            Overthere.getConnection(CIFS_PROTOCOL, options);

            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException expected) {
            assertThat(expected.getMessage(), containsString("Cannot open a cifs:winrm_internal: connection through an HTTP proxy"));
        }
    }

}
