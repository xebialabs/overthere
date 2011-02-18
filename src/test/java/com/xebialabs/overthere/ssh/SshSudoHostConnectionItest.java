package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import org.junit.Test;

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class SshSudoHostConnectionItest extends SshSudoHostConnectionItestBase {

	@Override
	protected void setupConnection() {
		type = "ssh_sudo";
		options = new ConnectionOptions();
		options.set("address", "was-61");
		options.set("username", "root");
		options.set("password", "centos");
		options.set("sudoUsername", "autodpl");
		options.set("os", UNIX);
	}

	@Test
	public void hostSessionIsAnSshSudoHostSession() {
		assertThat(connection, instanceOf(SshSudoHostConnection.class));
	}


}
