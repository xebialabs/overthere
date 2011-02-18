package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.*;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SshSftpHostSessionOnWindowsItest extends SshSudoHostConnectionItestBase {

	@Override
	protected void setupConnection() {
		type = "ssh_sftp";
		options.set("address", "win-xp");
		options.set("username", "Administrator");
		options.set("password", "deployit");
		options.set("temporaryDirectoryPath", "c:\\temp");
		options.set("os", OperatingSystemFamily.WINDOWS);
	}

	@Test
	@Ignore("Needs Windows image that is not on dexter")
	public void writeTemporaryFileAndTypeIt() {
		final String expectedOutput = "Mary had a little lamb";

		HostConnection hs = Overthere.getConnection(type, options);
		try {
			HostFile tempFile = hs.getTempFile("testoutput", ".txt");

			HostFileUtils.putStringToHostFile(expectedOutput + "\r\nwhose fleece was white as snow\r\n", tempFile);

			CapturingCommandExecutionCallbackHandler capturedOutput = new CapturingCommandExecutionCallbackHandler();
			final String p = tempFile.getPath();
			System.out.println("-->" + capturedOutput);
			hs.execute(capturedOutput, "type", p);

			assertTrue(capturedOutput.getOutput().contains(expectedOutput));
		} finally {
			hs.close();
		}
	}

	@Test
	@Ignore("Needs Windows image that is not on dexter")
	public void mkdirs() {

		HostConnection hs = Overthere.getConnection(type, options);
		try {
			HostFile tempFile3 = hs.getFile("c:\\temp\\level1\\level2\\level3");
			tempFile3.mkdirs();

			final HostFile file = tempFile3.getFile("foo.txt");

			HostFileUtils.putStringToHostFile("hello....\r\n", file);

		} finally {
			hs.close();
		}
	}

}
