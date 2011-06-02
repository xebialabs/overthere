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

import java.io.InputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.spi.Protocol;

/**
 * A connection to a remote host using SSH w/ interactive SUDO.
 */
@Protocol(name = "ssh_interactive_sudo")
public class SshInteractiveSudoOverthereConnection extends SshSudoOverthereConnection {

	public SshInteractiveSudoOverthereConnection(String type, ConnectionOptions options) {
		super(type, options);
	}

	@Override
	protected ChannelExec createExecChannel() throws JSchException {
		ChannelExec channel = super.createExecChannel();
		// FIXME: Move this to a connection option
		channel.setPty(true);
		return channel;
	}

	@Override
	protected OverthereProcess createProcess(final String command, final ChannelExec channel) {
		return new ChannelExecProcess(channel, command) {
			@Override
			public InputStream getStdout() {
				return new SshInteractiveSudoPasswordHandlingStream(super.getStdout(), getStdin(), password);
			}
		};
	}

}
