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
/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
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

