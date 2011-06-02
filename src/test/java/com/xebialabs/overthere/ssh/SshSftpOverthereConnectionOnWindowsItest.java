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

import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static com.xebialabs.overthere.util.OverthereUtils.write;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.junit.Ignore;
import org.junit.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;

@Ignore("Needs Windows image that is not on dexter")
public class SshSftpOverthereConnectionOnWindowsItest extends SshSudoOverthereConnectionItestBase {

	@Override
	protected void setTypeAndOptions() {
		type = "ssh_sftp";
		options.set("address", "win-xp");
		options.set("username", "Administrator");
		options.set("password", "deployit");
		options.set("temporaryDirectoryPath", "c:\\temp");
		options.set("os", OperatingSystemFamily.WINDOWS);
	}

	@Test
	public void writeTemporaryFileAndTypeIt() {
		final String expectedOutput = "Mary had a little lamb";

		OverthereConnection hs = Overthere.getConnection(type, options);
		try {
			OverthereFile tempFile = hs.getTempFile("testoutput", ".txt");
			write(expectedOutput + "\r\nwhose fleece was white as snow\r\n", "UTF-8", tempFile);

			CapturingOverthereProcessOutputHandler capturedOutput = capturingHandler();
			final String p = tempFile.getPath();
			System.out.println("-->" + capturedOutput);
			hs.execute(capturedOutput, CmdLine.build("type", p));

			assertThat(capturedOutput.getOutput(), containsString(expectedOutput));
		} finally {
			hs.disconnect();
		}
	}

	@Test
	public void mkdirs() {

		OverthereConnection hs = Overthere.getConnection(type, options);
		try {
			OverthereFile tempFile3 = hs.getFile("c:\\temp\\level1\\level2\\level3");
			tempFile3.mkdirs();

			final OverthereFile file = tempFile3.getFile("foo.txt");

			write("hello....\r\n", "UTF-8", file);
		} finally {
			hs.disconnect();
		}
	}

}
