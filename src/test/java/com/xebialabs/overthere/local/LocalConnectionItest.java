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

import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.testng.annotations.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnectionItestBase;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

public class LocalConnectionItest extends OverthereConnectionItestBase {

    @Override
	protected void doInitHost() {
	}

	@Override
	protected void doTeardownHost() {
	}

	@Override
	protected void setTypeAndOptions() {
		protocol = LOCAL_PROTOCOL;
		options = new ConnectionOptions();
		options.set(TEMPORARY_DIRECTORY_PATH, temp.getRoot().getPath());
		expectedConnectionClassName = LocalConnection.class.getName();
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

	@Test
	public void localFileShouldBeSerializable() throws IOException, ClassNotFoundException {
		OverthereFile tempFile = connection.getTempFile("afile");
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream objectsOut = new ObjectOutputStream(bytes);
		objectsOut.writeObject(tempFile);
		
		ObjectInputStream objectsIn = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
		Object read = objectsIn.readObject();
		assertThat(read, instanceOf(LocalFile.class));
		assertThat(((LocalFile) read).getPath(), equalTo(tempFile.getPath()));
	}

}

