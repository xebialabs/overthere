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

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnectionItestBase;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

public class LocalOverthereConnectionItest extends OverthereConnectionItestBase {

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
		tempFile.mkdir();
		assertThat("expected temp is a dir", tempFile.isDirectory(), equalTo(true));
	}

	@Test
	public void canExecuteCommand() {
		OverthereFile tempFile = connection.getTempFile("afile");
		OverthereUtils.write("Some text", "UTF-8", tempFile);
		String lsCommand = connection.getHostOperatingSystem() == UNIX ? "ls" : "dir";
		CmdLine commandLine = CmdLine.build(lsCommand, tempFile.getParentFile().getPath());
		CapturingOverthereProcessOutputHandler handler = capturingHandler();

		int res = connection.execute(handler, commandLine);
		assertThat(res, equalTo(0));
		assertThat(handler.getOutputLines().contains(tempFile.getName()), equalTo(true));
	}

}
