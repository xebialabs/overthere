/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
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

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;

import com.xebialabs.overthere.util.DefaultAddressPortMapper;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.Factory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

public class PresetClientSshConnection extends SshConnection {

    public PresetClientSshConnection(ConnectionOptions options, final SSHClient clientToReturn) {
        super(SSH_PROTOCOL, options, new DefaultAddressPortMapper());
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