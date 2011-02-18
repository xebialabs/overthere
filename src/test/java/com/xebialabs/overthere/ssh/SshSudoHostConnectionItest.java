package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.xebialabs.overthere.ConnectionOptions;

public class SshSudoHostConnectionItest extends SshSudoHostConnectionItestBase {

	@Override
    protected void setTypeAndOptions() {
		type = "ssh_sudo";
		options = new ConnectionOptions();
		options.set("address", "overthere");
		options.set("username", "trusted");
		options.set("password", "trustme");
		options.set("sudoUsername", "overthere");
		options.set("os", UNIX);
	}

	@Test
	public void hostSessionIsAnSshSudoHostSession() {
		assertThat(connection, instanceOf(SshSudoHostConnection.class));
	}

}
