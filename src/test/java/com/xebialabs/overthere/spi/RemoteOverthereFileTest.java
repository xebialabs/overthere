package com.xebialabs.overthere.spi;

import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;

public class RemoteOverthereFileTest {

	@Test
	public void shouldNotNormalizeUnixRemoteFilePath() {
		RemoteOverthereFile remoteFile = getDummyRemoteFile("/tmp/somefile");
		Assert.assertThat(remoteFile.getPath(), is("/tmp/somefile"));
	}

	@Test
	public void shouldNotNormalizeWindowsRemoteFilePath() {
		RemoteOverthereFile remoteFile = getDummyRemoteFile("\\tmp\\somefile");
		Assert.assertThat(remoteFile.getPath(), is("\\tmp\\somefile"));
	}

	private RemoteOverthereFile getDummyRemoteFile(String filepath) {
		return new RemoteOverthereFile(null, filepath) {

			@Override
			public OutputStream put(long length) {
				return null;
			}

			@Override
			public InputStream get() {
				return null;
			}

			@Override
			public boolean renameTo(File dest) {
				return false;
			}

			@Override
			public boolean mkdirs() {
				return false;
			}

			@Override
			public boolean mkdir() {
				return false;
			}

			@Override
			public String[] list() {
				return null;
			}

			@Override
			public long length() {
				return 0;
			}

			@Override
			public long lastModified() {
				return 0;
			}

			@Override
			public boolean isHidden() {
				return false;
			}

			@Override
			public boolean isDirectory() {
				return false;
			}

			@Override
			public boolean exists() {
				return false;
			}

			@Override
			public boolean delete() {
				return false;
			}

			@Override
			public boolean canWrite() {
				return false;
			}

			@Override
			public boolean canRead() {
				return false;
			}

			@Override
			public boolean canExecute() {
				return false;
			}
		};
	}
}
