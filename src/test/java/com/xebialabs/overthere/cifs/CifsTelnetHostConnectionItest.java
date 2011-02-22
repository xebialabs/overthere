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
import com.xebialabs.overthere.HostFile;
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
		HostFile file = connection.getFile("C:");
		List<String> filesInC = file.list();
		assertTrue(filesInC.contains("AUTOEXEC.BAT"));
	}

	@Test
	public void readFile() throws IOException {
		HostFile file = connection.getFile("C:\\itest\\itestfile.txt");
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

