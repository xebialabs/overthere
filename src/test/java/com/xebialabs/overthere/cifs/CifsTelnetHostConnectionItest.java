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
package com.xebialabs.overthere.cifs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.HostSessionItestBase;
import com.xebialabs.overthere.OperatingSystemFamily;

@Ignore("Needs Windows 2003 image")
public class CifsTelnetHostConnectionItest extends HostSessionItestBase {

	@Override
    protected void setTypeAndOptions() {
		type = "cifs_telnet";
		options = new ConnectionOptions();
		options.set("address", "wls-11g-win");
		options.set("username", "itestuser");
		// ensure the test user contains some reserved characters such as ';', ':' or '@' 
		options.set("password", "hello@:;<>myfriend");
		options.set("os", OperatingSystemFamily.WINDOWS);
	}

	@Test
	public void listC() throws IOException {
		OverthereFile file = connection.getFile("C:");
		List<String> filesInC = file.list();
		assertTrue(filesInC.contains("AUTOEXEC.BAT"));
	}

	@Test
	public void readFile() throws IOException {
		OverthereFile file = connection.getFile("C:\\itest\\itestfile.txt");
		assertEquals("itestfile.txt", file.getName());
		assertEquals(27, file.length());
		InputStream inputStream = file.get();
		ByteArrayOutputStream fileContents = new ByteArrayOutputStream();
		try {
			IOUtils.copy(inputStream, fileContents);
		} finally {
			inputStream.close();
		}
		assertTrue(fileContents.toString().contains("And the mome raths outgrabe"));
	}

	@Test
	public void executeDirCommand() {
		CapturingCommandExecutionCallbackHandler handler = new CapturingCommandExecutionCallbackHandler(true);
		int res = connection.execute(handler, "dir", "C:\\itest");
		assertEquals(0, res);
		assertTrue(handler.getOutput().contains("27 itestfile.txt"));
	}

	@Test
	public void executeCmdCommand() {
		CapturingCommandExecutionCallbackHandler handler = new CapturingCommandExecutionCallbackHandler(true);
		int res = connection.execute(handler, "C:\\itest\\itestecho.cmd");
		assertEquals(0, res);
		assertTrue(handler.getOutput().contains("All mimsy were the borogroves"));
	}

	@Test
	public void executeIncorrectCommand() {
		CapturingCommandExecutionCallbackHandler handler = new CapturingCommandExecutionCallbackHandler(true);
		int res = connection.execute(handler, "C:\\NONEXISTANT.cmd");
		assertEquals(9009, res);
	}

}

