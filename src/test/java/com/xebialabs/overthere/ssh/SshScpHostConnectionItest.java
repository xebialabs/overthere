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

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;

public class SshScpHostConnectionItest extends SshHostConnectionItestBase {

	@Override
    protected void setTypeAndOptions() {
		type = "ssh_scp";
		options = new ConnectionOptions();
		options.set("address", "overthere");
		options.set("username", "overthere");
		options.set("password", "overhere");
		options.set("os", OperatingSystemFamily.UNIX);
	}

	@Test
	public void hostSessionIsAnSshScpHostSession() {
		assertThat(connection, instanceOf(SshScpHostConnection.class));
	}

}

