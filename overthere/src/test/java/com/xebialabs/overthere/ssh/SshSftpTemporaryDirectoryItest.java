package com.xebialabs.overthere.ssh;

import com.xebialabs.itest.ItestHost;
import com.xebialabs.itest.ItestHostFactory;
import com.xebialabs.overthere.*;
import com.xebialabs.overthere.util.OverthereUtils;
import net.schmizz.sshj.sftp.FileAttributes;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class SshSftpTemporaryDirectoryItest {

	private static ItestHost host;

	@BeforeClass
	public static void setupHost() {
		host = ItestHostFactory.getItestHost("overthere-unix");
		host.setup();
	}

	@AfterClass
	public static void tearDownHost() {
		host.teardown();
	}
	
	@Test
	public void shouldBeAbleToDeleteTemporaryDirectoryOnExit() {
		String password = "overhere";
		String server = host.getHostName();
		String username = "overthere";
		ConnectionOptions options = new ConnectionOptions();
		options.set(ADDRESS, server);
		options.set(USERNAME, username);
		options.set(PASSWORD, password);
		options.set(OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
		options.set("connectionType", SshConnectionType.SFTP);
		options.set(TEMPORARY_DIRECTORY_PATH, "/tmp");

		OverthereConnection connection = Overthere.getConnection("ssh", options);

		OverthereFile tempDir = connection.getTempFile("testSFTP",".tmp");
		tempDir.mkdir();
		assertThat(tempDir.isDirectory(), equalTo(true));
		OverthereFile parentFile = tempDir.getParentFile();
		FileAttributes stat = ((SshSftpFile) parentFile).stat();
		logger.info("Statted {} --> {}", parentFile, stat);
		assertThat(parentFile.isDirectory(), equalTo(true));
		OverthereFile file = tempDir.getFile("afile");
		OverthereUtils.write("the content", "UTF-8", file);

		connection.close();
	}

	private static final Logger logger = LoggerFactory.getLogger(SshSftpTemporaryDirectoryItest.class);
}
