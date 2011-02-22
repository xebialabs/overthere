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

package com.xebialabs.overthere.local;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.internal.matchers.StringContains.containsString;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.DebugCommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.HostSessionItestBase;
import com.xebialabs.overthere.OperatingSystemFamily;

public class LocalHostConnectionTest extends HostSessionItestBase {

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	@Override
    protected void setTypeAndOptions() {
		type = "local";
		options = new ConnectionOptions();
		options.set("temporaryDirectoryPath", temp.getRoot().getPath());
	}

	@Test
	public void isDirectoryWorks() {
		HostFile tempFile = connection.getTempFile("tmpDir");
		tempFile.delete();
		tempFile.mkdir();
		assertTrue("expected temp is a dir", tempFile.isDirectory());
	}

	@Test
	@Ignore
	// FIXME: needs different test command
	public void passwordNotSeen() {
		CapturingCommandExecutionCallbackHandler handler = new CapturingCommandExecutionCallbackHandler();
		connection.execute(handler, "foo.sh -username", "benoit", "-password", "benoit");
		assertThat(handler.getOutput(), containsString("********"));
	}

	@Test
	@Ignore
	// FIXME: needs different test command
	public void passwordNotSeen2() {
		try {
			connection.execute(new DebugCommandExecutionCallbackHandler(), "foo.sh -username benoit -password benoit");
		} catch (Throwable t) {

		}
	}

}

