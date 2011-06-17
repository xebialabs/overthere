/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.ssh;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.InputStream;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.spi.Protocol;

/**
 * A connection to a remote host using SSH w/ interactive SUDO.
 */
@Protocol(name = "ssh_interactive_sudo")
public class SshInteractiveSudoConnection extends SshSudoConnection {

	public SshInteractiveSudoConnection(String type, ConnectionOptions options) {
		super(type, options);
		checkArgument(password != null, "Cannot start an interactive SSH SUDO connection without a password");
	}

    @Override
    protected SshProcess createProcess(final Session session, final CmdLine commandLine) throws TransportException, ConnectionException {
        return new SshProcess(this, session, commandLine) {
            @Override
            protected Session.Command startCommand() throws TransportException, ConnectionException {
                session.allocateDefaultPTY();
                return super.startCommand();
            }

            @Override
            public InputStream getStdout() {
                return new SshInteractiveSudoPasswordHandlingStream(super.getStdout(), getStdin(), password);
            }
        };
    }
}
