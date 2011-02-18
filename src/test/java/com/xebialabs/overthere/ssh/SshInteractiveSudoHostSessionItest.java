package com.xebialabs.overthere.ssh;

import static com.xebialabs.deployit.ci.HostAccessMethod.SSH_INTERACTIVE_SUDO;
import static com.xebialabs.deployit.ci.OperatingSystemFamily.UNIX;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xebialabs.deployit.ci.Host;

public class SshInteractiveSudoHostSessionItest extends SshSudoHostSessionItestBase {

	@Override
	protected void setupTargetHost() {
		targetHost = new Host();
		targetHost.setAddress("was-61");
		targetHost.setUsername("autodpl");
		targetHost.setPassword("autodpl");
		targetHost.setSudoUsername("root");
		targetHost.setOperatingSystemFamily(UNIX);
		targetHost.setAccessMethod(SSH_INTERACTIVE_SUDO);
	}

	@Test
	public void hostSessionIsAnSshSudoHostSession() {
		assertEquals(SshInteractiveSudoHostSession.class, session.getClass());
	}


}
