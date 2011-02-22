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

import org.junit.After;
import org.junit.Ignore;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.HostSessionItestBase;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;

@Ignore("Needs image with user that is identified by key")
public class SshIdentifiedByPubKeyItest extends HostSessionItestBase {

	@Override
    protected void setTypeAndOptions() {
		options = new ConnectionOptions();
		options.set("address", "jboss-51");
		options.set("username", "autodpl");

		System.setProperty("ssh.privatekey.filename", System.getProperty("user.home") + "/.ssh/deployit-itest-id_rsa");
		options.set("os", OperatingSystemFamily.UNIX);
		options.set("temporaryDirectoryPath", "/tmp");
		type = "ssh_scp";
		connection = Overthere.getConnection(type, options);
	}

	@After
	public void clean() {
		System.clearProperty("ssh.privatekey.filename");
	}

}

