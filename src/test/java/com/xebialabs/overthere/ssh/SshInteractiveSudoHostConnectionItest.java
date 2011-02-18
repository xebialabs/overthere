package com.xebialabs.overthere.ssh;
import com.xebialabs.overthere.ConnectionOptions;
import org.junit.Test;

import static com.xebialabs.overthere.OperatingSystemFamily.*;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SshInteractiveSudoHostConnectionItest extends SshSudoHostConnectionItestBase {

	@Override
	protected void setupConnection() {
		type = "ssh_interactive_sudo";
		options = new ConnectionOptions();
		options.set("address", "was-61");
		options.set("username", "autodpl");
		options.set("password", "autodpl");
		options.set("sudoUsername", "root");
		options.set("os", UNIX);
	}

	@Test
	public void hostSessionIsAnSshSudoHostSession() {
		assertThat(connection, instanceOf(SshInteractiveSudoHostConnection.class));

	}


}
