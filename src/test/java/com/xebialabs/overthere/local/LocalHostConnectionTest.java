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
import com.xebialabs.overthere.OverthereFile;
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
		OverthereFile tempFile = connection.getTempFile("tmpDir");
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

