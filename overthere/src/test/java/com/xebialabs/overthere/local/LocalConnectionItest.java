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
import static com.xebialabs.overthere.util.CapturingOverthereProcessOutputHandler.capturingHandler;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.io.*;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
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
		type = "local";
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
	public void localFileIsSerializable() throws IOException, ClassNotFoundException {
		OverthereFile tempFile = connection.getTempFile("afile");
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream objectsOut = new ObjectOutputStream(bytes);
		objectsOut.writeObject(tempFile);
		
		ObjectInputStream objectsIn = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
		Object read = objectsIn.readObject();
		assertThat(read, instanceOf(LocalFile.class));
		assertThat(((LocalFile) read).getPath(), equalTo(tempFile.getPath()));
	}

	@Test
	public void shouldTruncateExistingTargetFileOnCopy() throws Exception {
		final OverthereFile existingDestination = connection.getTempFile("existing");
		writeData(existingDestination, "**********\n**********\n**********\n**********\n**********\n".getBytes());
		final OverthereFile newSource = connection.getTempFile("newContents");
		writeData(newSource, "++++++++++".getBytes());
		newSource.copyTo(existingDestination);

		byte[] read = new byte[1024];
		ByteArrayOutputStream to = new ByteArrayOutputStream();
		ByteStreams.copy(new InputSupplier<InputStream>() {
			@Override
			public InputStream getInput() throws IOException {
				return existingDestination.getInputStream();
			}
		}, to);
		byte[] bytes = to.toByteArray();
		assertThat(bytes.length, equalTo(10));
		assertThat(bytes, equalTo("++++++++++".getBytes()));
	}

	private void writeData(final OverthereFile tempFile, byte[] data) throws IOException {
		ByteStreams.write(data, new OutputSupplier<OutputStream>() {
			@Override
			public OutputStream getOutput() throws IOException {
				return tempFile.getOutputStream();
			}
		});
	}
}

