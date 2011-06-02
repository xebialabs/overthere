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

import com.xebialabs.overthere.ConnectionOptions;
import org.junit.Test;

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class SshSftpOverthereConnectionItest extends SshOverthereConnectionItestBase {

	@Override
    protected void setTypeAndOptions() {
		type = "ssh_sftp";
		options = new ConnectionOptions();
		options.set("address", "overthere");
		options.set("username", "overthere");
		options.set("password", "overhere");
		options.set("os", UNIX);
	}

	@Test
	public void hostSessionIsAnSshSftpHostSession() {
		assertThat(connection, instanceOf(SshSftpOverthereConnection.class));
	}

}

