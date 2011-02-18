package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OperatingSystemFamily;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SshScpHostConnectionItest extends SshHostConnectionItestBase {

	@Override
    protected void setTypeAndOptions() {
		type = "ssh_scp";
		options = new ConnectionOptions();
		options.set("address", "overthere");
		options.set("username", "overthere");
		options.set("password", "overhere");
		options.set("os", OperatingSystemFamily.UNIX);
	}

	@Test
	public void hostSessionIsAnSshScpHostSession() {
		assertThat(connection, is(SshScpHostConnection.class));
	}

}
