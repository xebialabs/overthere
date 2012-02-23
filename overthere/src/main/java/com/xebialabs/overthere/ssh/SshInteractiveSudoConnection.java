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

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_PASSWORD_PROMPT_REGEX_DEFAULT;

/**
 * A connection to a Unix host using SSH w/ interactive SUDO.
 */
class SshInteractiveSudoConnection extends SshSudoConnection {

	private String passwordPromptRegex;

	public SshInteractiveSudoConnection(String type, ConnectionOptions options) {
		super(type, options);
		passwordPromptRegex = options.get(SUDO_PASSWORD_PROMPT_REGEX, SUDO_PASSWORD_PROMPT_REGEX_DEFAULT);
		checkArgument(!passwordPromptRegex.endsWith("*"), SUDO_PASSWORD_PROMPT_REGEX + " should not end in a wildcard");
		checkArgument(!passwordPromptRegex.endsWith("?"), SUDO_PASSWORD_PROMPT_REGEX + " should not end in a wildcard");
		checkArgument(password != null, "Cannot start a ssh:" + sshConnectionType.toString().toLowerCase() + ": connection without a password");
	}

    @Override
    protected SshProcess createProcess(final Session session, final CmdLine commandLine) throws TransportException, ConnectionException {
        return new SshProcess(this, os, session, commandLine) {
            @Override
            public InputStream getStdout() {
                return new SshInteractiveSudoPasswordHandlingStream(super.getStdout(), getStdin(), password, passwordPromptRegex);
            }
        };
    }
}
