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
package com.xebialabs.overthere;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class HostSessionItestBase {

	protected ConnectionOptions options;

	protected String type;

	protected HostConnection connection;

	@Before
	public void connect() {
		setTypeAndOptions();
		connection = Overthere.getConnection(type, options);
	}

	protected abstract void setTypeAndOptions();

	@After
	public void disconnect() {
		connection.disconnect();
	}

	@Test
	public void createWriteReadAndRemoveTemporaryFile() throws IOException {
		final String prefix = "prefix";
		final String suffix = "suffix";
		final byte[] contents = ("Contents of the temporary file created at " + System.currentTimeMillis() + "ms since the epoch").getBytes();

		OverthereFile tempFile = connection.getTempFile(prefix, suffix);
		assertThat("Expected a non-null return value from HostConnection.getTempFile()", tempFile, notNullValue());
		assertThat("Expected name of temporary file to start with the prefix", tempFile.getName(), startsWith(prefix));
		assertThat("Expected name of temporary file to end with the suffix", tempFile.getName(), endsWith(suffix));
		assertThat("Expected temporary file to not exist yet", tempFile.exists(), equalTo(false));

		OutputStream outputStream = tempFile.put(contents.length);
		try {
			outputStream.write(contents);
		} finally {
			outputStream.close();
		}

		assertThat("Expected temporary file to exist after writing to it", tempFile.exists(), equalTo(true));
		assertThat("Expected temporary file to not be a directory", tempFile.isDirectory(), equalTo(false));
		assertThat("Expected temporary file to have the size of the contents written to it", tempFile.length(), equalTo((long) contents.length));
		assertThat("Expected temporary file to be readable", tempFile.canRead(), equalTo(true));
		assertThat("Expected temporary file to be writeable", tempFile.canWrite(), equalTo(true));

		// Windows systems don't support the concept of checking for executability
		if (connection.getHostOperatingSystem() == OperatingSystemFamily.UNIX) {
			assertFalse("Expected temporary file to not be executable", tempFile.canExecute());
		}

		DataInputStream inputStream = new DataInputStream(tempFile.get());
		try {
			final byte[] contentsRead = new byte[contents.length];
			inputStream.readFully(contentsRead);
			assertThat("Expected input stream to be exhausted after reading the full contents", inputStream.available(), equalTo(0));
			assertThat("Expected contents in temporary file to be identical to data written into it", contentsRead, equalTo(contents));
		} finally {
			inputStream.close();
		}

		tempFile.delete();
		assertThat("Expected temporary file to no longer exist", tempFile.exists(), equalTo(false));
	}

	@Test
	public void createPopulateListAndRemoveTemporaryDirectory() throws IOException {
		final String prefix = "prefix";
		final String suffix = "suffix";

		OverthereFile tempDir = connection.getTempFile(prefix, suffix);
		assertThat("Expected a non-null return value from HostConnection.getTempFile()", tempDir, notNullValue());
		assertThat("Expected name of temporary file to start with the prefix", tempDir.getName(), startsWith(prefix));
		assertThat("Expected name of temporary file to end with the suffix", tempDir.getName(), endsWith(suffix));
		assertThat("Expected temporary file to not exist yet", tempDir.exists(), equalTo(false));

		tempDir.mkdir();
		assertThat("Expected temporary directory to exist after creating it", tempDir.exists(), equalTo(true));
		assertThat("Expected temporary directory to be a directory", tempDir.isDirectory(), equalTo(true));

		OverthereFile anotherTempDir = connection.getTempFile(prefix, suffix);
		assertThat("Expected temporary directories created with identical prefix and suffix to still be different", tempDir.getPath(),
		        not(equalTo(anotherTempDir.getPath())));

		OverthereFile nested1 = tempDir.getFile("nested1");
		OverthereFile nested2 = nested1.getFile("nested2");
		OverthereFile nested3 = nested2.getFile("nested3");
		assertThat("Expected deeply nested directory to not exist", nested3.exists(), equalTo(false));
		// FIXME: Remove either the if or the catch when the error handling strategy for mkdir is decided
		try {
			if (nested3.mkdir()) {
				fail("Expected not to be able to create a deeply nested directory in one go");
			}
		} catch (RuntimeIOException expected1) {
		}
		assertThat("Expected deeply nested directory to still not exist", nested3.exists(), equalTo(false));
		nested3.mkdirs();
		assertThat("Expected deeply nested directory to exist after invoking mkdirs on it", nested3.exists(), equalTo(true));

		final byte[] contents = ("Contents of the temporary file created at " + System.currentTimeMillis() + "ms since the epoch").getBytes();
		OverthereFile regularFile = tempDir.getFile("somefile.txt");
		regularFile.put(new ByteArrayInputStream(contents), contents.length);

		String[] dirContents = tempDir.list();
		assertThat("Expected directory to contain two entries", dirContents.length, equalTo(2));
		assertThat("Expected directory to contain parent of deeply nested directory", dirContents, hasItemInArray(nested1.getName()));
		assertThat("Expected directory to contain regular file that was just created", dirContents, hasItemInArray(regularFile.getName()));

		// FIXME: Remove either the if or the catch when the error handling strategy for mkdir is decided
		try {
			if (nested1.delete()) {
				fail("Expected to not be able to remove a non-empty directory");
			}
		} catch (RuntimeIOException expected2) {
		}
		nested1.deleteRecursively();
		assertThat("Expected parent of deeply nested directory to have been removed recursively", nested1.exists(), equalTo(false));

		regularFile.delete();
		tempDir.delete();
		assertThat("Expected temporary directory to not exist after removing it when it was empty", tempDir.exists(), equalTo(false));
	}

}
