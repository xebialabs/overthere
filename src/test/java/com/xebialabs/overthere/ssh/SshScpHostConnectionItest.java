package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SshScpHostConnectionItest extends SshHostConnectionItestBase {

	@Override
	protected void setupConnection() {
		type = "ssh_scp";
		options = new ConnectionOptions();
		options.set("address", "apache-22");
		options.set("username", "root");
		options.set("password", "centos");
		options.set("os", OperatingSystemFamily.UNIX);
	}

	@Test
	public void hostSessionIsAnSshScpHostSession() {
		assertThat(connection, is(SshScpHostConnection.class));
	}

}
