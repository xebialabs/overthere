package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.xebialabs.overthere.ConnectionOptions;

public class SshInteractiveSudoHostConnectionItest extends SshSudoHostConnectionItestBase {

	@Override
    protected void setTypeAndOptions() {
		type = "ssh_interactive_sudo";
		options = new ConnectionOptions();
		options.set("address", "overthere");
		options.set("username", "untrusted");
		options.set("password", "donttrustme");
		options.set("sudoUsername", "overthere");
		options.set("os", UNIX);
	}

	@Test
	public void hostSessionIsAnSshSudoHostSession() {
		assertThat(connection, instanceOf(SshInteractiveSudoHostConnection.class));
	}

}
