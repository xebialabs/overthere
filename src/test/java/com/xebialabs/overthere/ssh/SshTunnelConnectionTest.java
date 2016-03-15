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

import java.net.ServerSocket;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SshTunnelConnectionTest {

    @Test
    public void shouldNotHandOutDuplicatePortsWhenSocketsAlwaysSeemToBeFreeLikeOnWindows() {
        SshTunnelConnection.TunnelPortManager tunnelPortManager = new SshTunnelConnection.TunnelPortManager() {
            @Override
            protected ServerSocket tryBind(int localPort) {
                ServerSocket mock = mock(ServerSocket.class);
                when(mock.getLocalPort()).thenReturn(localPort);
                return mock;
            }
        };

        ServerSocket serverSocket = tunnelPortManager.bindToNextFreePort(1025);
        ServerSocket serverSocket2 = tunnelPortManager.bindToNextFreePort(1025);
        assertThat(serverSocket.getLocalPort(), Matchers.not(equalTo(serverSocket2.getLocalPort())));
    }
}
