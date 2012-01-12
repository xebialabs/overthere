/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.local;

import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnectionItestBase;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler;
import com.xebialabs.overthere.util.OverthereUtils;

public class LocalConnectionItest extends OverthereConnectionItestBase {

	@Override
	protected void setTypeAndOptions() {
		type = LOCAL_PROTOCOL;
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


