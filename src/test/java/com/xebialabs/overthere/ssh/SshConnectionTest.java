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

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import net.schmizz.keepalive.KeepAlive;
import net.schmizz.sshj.MockitoFriendlySSHClient;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.Connection;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.Transport;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthMethod;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SshConnection}
 */
public class SshConnectionTest {

    @Mock
    MockitoFriendlySSHClient client;

    private ConnectionOptions connectionOptions;

    private Session session;
    private Connection conn;
    private KeepAlive keepAlive;

    @BeforeMethod
    public void init() throws ConnectionException, TransportException {
        MockitoAnnotations.initMocks(this);
        connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SFTP);
        connectionOptions.set(OPERATING_SYSTEM, UNIX);
        connectionOptions.set(ADDRESS, "nowhere.example.com");
        connectionOptions.set(USERNAME, "some-user");
        session = mock(Session.class);
        when(client.startSession()).thenReturn(session);
        conn = mock(Connection.class);
        keepAlive = mock(KeepAlive.class);
        when(client.getConnection()).thenReturn(conn);
        when(conn.getKeepAlive()).thenReturn(keepAlive);
    }

    @Test
    public void shouldUsePasswordIfNoKeyfile() throws IOException {
        String password = "secret";
        connectionOptions.set(PASSWORD, password);
        SshConnection connection = newConnectionWithClient(client);
        connection.connect();

        verify(client).auth(eq("some-user"), isA(AuthMethod.class), isA(AuthMethod.class));
        connection.close();

    }

    @Test
    public void shouldUseKeyfileIfNoPassword() throws IOException {
        connectionOptions.set(PRIVATE_KEY_FILE, "/path/to/keyfile");
        SshConnection connection = newConnectionWithClient(client);
        connection.connect();

        verify(client).authPublickey(eq("some-user"), Matchers.<KeyProvider>anyVararg());
        connection.close();

    }

    @Test
    public void keyfileShouldOverridePassword() throws IOException {
        String password = "secret";
        connectionOptions.set(PASSWORD, password);
        connectionOptions.set(PRIVATE_KEY_FILE, "/path/to/keyfile");
        SshConnection connection = newConnectionWithClient(client);
        connection.connect();

        verify(client).authPublickey(eq("some-user"), Matchers.<KeyProvider>anyVararg());
        verify(client, never()).authPassword(anyString(), anyString());
        connection.close();
    }

    @Test
    public void keyShouldOverrideKeyFile() throws IOException {
        String keyFile = "/path/to/keyfile";
        connectionOptions.set(PRIVATE_KEY_FILE, keyFile);
        connectionOptions.set(PRIVATE_KEY, "RANDOM");
        SshConnection connection = newConnectionWithClient(client);
        connection.connect();

        verify(client).authPublickey(eq("some-user"), Matchers.<KeyProvider>anyVararg());
        verify(client, never()).authPublickey(Matchers.eq(keyFile), anyString());
        connection.close();
    }

    @Test
    public void shouldNotAllocateDefaultPty() throws IOException {
        connectionOptions.set(ALLOCATE_DEFAULT_PTY, false);

        SshConnection connection = newConnectionWithClient(client);
        connection.connect();
        connection.startProcess(CmdLine.build("dummy"));

        verify(session, times(0)).allocateDefaultPTY();
        connection.close();
    }

    @Test
    public void shouldAllocateDefaultPty() throws IOException {
        connectionOptions.set(ALLOCATE_DEFAULT_PTY, true);

        SshConnection connection = newConnectionWithClient(client);
        connection.connect();
        connection.startProcess(CmdLine.build("dummy"));

        verify(session).allocateDefaultPTY();
        connection.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldAllocatePty() throws IOException {
        connectionOptions.set(ALLOCATE_PTY, "xterm:132:50:264:100");

        SshConnection connection = newConnectionWithClient(client);
        connection.connect();
        connection.startProcess(CmdLine.build("dummy"));

        verify(session).allocatePTY(eq("xterm"), eq(132), eq(50), eq(264), eq(100), anyMap());
        connection.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void allocatePtyShouldOverrideAllocateDefaultPty() throws IOException {
        connectionOptions.set(ALLOCATE_PTY, "xterm:132:50:264:100");
        connectionOptions.set(ALLOCATE_DEFAULT_PTY, true);

        SshConnection connection = newConnectionWithClient(client);
        connection.connect();
        connection.startProcess(CmdLine.build("dummy"));

        verify(session).allocatePTY(eq("xterm"), eq(132), eq(50), eq(264), eq(100), anyMap());
        verify(session, times(0)).allocateDefaultPTY();
        connection.close();

    }

    @Test
    public void shouldUseHeartbeat() throws IOException {
        connectionOptions.set(HEARTBEAT_INTERVAL, 30);
        SshConnection connection = newConnectionWithClient(client);
        connection.connect();

        verify(keepAlive, times(1)).setKeepAliveInterval(30);
        connection.close();
    }

    @Test
    public void shouldUseDefaultHeartbeat() throws IOException {
        SshConnection connection = newConnectionWithClient(client);
        connection.connect();

        verify(keepAlive, times(1)).setKeepAliveInterval(0);
        connection.close();
    }

    private SshConnection newConnectionWithClient(SSHClient client) {
        return new PresetClientSshConnection(connectionOptions, client);
    }

}
