/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PRIVATE_KEY_FILE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import net.schmizz.sshj.MockitoFriendlySSHClient;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.Factory;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import net.schmizz.sshj.userauth.method.AuthMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Unit tests for {@link SshConnection}
 */
public class SshConnectionTest {
    private ConnectionOptions connectionOptions;

    @Before
    public void init() {
        connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SFTP);
        connectionOptions.set(OPERATING_SYSTEM, UNIX);
        connectionOptions.set(ADDRESS, "nowhere.example.com");
        connectionOptions.set(USERNAME, "some-user");
    }
    
    @Test
    public void passwordIsUsedIfNoKeyfile() throws IOException {
        SSHClient client = mockClient();
        String password = "secret";
        connectionOptions.set(PASSWORD, password);
        newConnectionWithClient(client).connect();
        
        verify(client).auth(eq("some-user"), isA(AuthMethod.class), isA(AuthMethod.class));
    }

    @Test
    public void keyfileIsUsedIfNoPassword() throws IOException {
        SSHClient client = mockClient();
        connectionOptions.set(PRIVATE_KEY_FILE, "/path/to/keyfile");
        newConnectionWithClient(client).connect();
        
        verify(client).authPublickey(eq("some-user"), Matchers.<KeyProvider>anyVararg());
    }
    
    @Test
    public void keyfileOverridesPassword() throws IOException {
        SSHClient client = mockClient();
        String password = "secret";
        connectionOptions.set(PASSWORD, password);
        connectionOptions.set(PRIVATE_KEY_FILE, "/path/to/keyfile");
        newConnectionWithClient(client).connect();
        
        verify(client).authPublickey(eq("some-user"), Matchers.<KeyProvider>anyVararg());
        verify(client, never()).authPassword(anyString(), anyString());
    }
    
    private static SSHClient mockClient() throws IOException {
        SSHClient client = mock(MockitoFriendlySSHClient.class);
        doNothing().when(client).connect("nowhere.example.com", 22);
        return client;
    }
    
    private SshConnection newConnectionWithClient(SSHClient client) {
        return new PresetClientSshConnection(connectionOptions, client);
    }
    
    private static class PresetClientSshConnection extends SshConnection {

        public PresetClientSshConnection(ConnectionOptions options, final SSHClient clientToReturn) {
            super(SSH_PROTOCOL, options);
            sshClientFactory = new Factory<SSHClient>() {
                @Override
                public SSHClient create() {
                    return clientToReturn;
                }
            };
        }

        @Override
        protected OverthereFile getFile(String hostPath, boolean isTempFile)
                throws RuntimeIOException {
            throw new UnsupportedOperationException("TODO Auto-generated method stub");
        }
    }
}

