package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.util.OverthereUtils;
import net.schmizz.sshj.sftp.FileAttributes;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xebialabs.overthere.ConnectionOptions.*;

public class SshSftpTemporaryDirectoryTest {

	@Test
	public void shouldBeAbleToDeleteTemporaryDirectoryOnExit() {
		String password = "overhere";
		String server = "overthere";
		String username = "overthere";

		ConnectionOptions connectionOptions = new ConnectionOptions();
		connectionOptions.set(ADDRESS, server);
		connectionOptions.set(USERNAME, username);
		connectionOptions.set(PASSWORD, password);
		connectionOptions.set(OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
		connectionOptions.set("connectionType", SshConnectionType.SFTP);
		connectionOptions.set(TEMPORARY_DIRECTORY_PATH, "/tmp");

		OverthereConnection connection = Overthere.getConnection("ssh", connectionOptions);

		OverthereFile tempDir = connection.getTempFile("testSFTP",".tmp");
		tempDir.mkdir();

		OverthereFile parentFile = tempDir.getParentFile();
		FileAttributes stat = ((SshSftpFile) parentFile).stat();
		logger.info("Statted {} --> {}", parentFile, stat);
		
		OverthereFile file = tempDir.getFile("afile");
		OverthereUtils.write("the content", "UTF-8", file);

		connection.close();
	}

	private static final Logger logger = LoggerFactory.getLogger(SshSftpTemporaryDirectoryTest.class);
}
