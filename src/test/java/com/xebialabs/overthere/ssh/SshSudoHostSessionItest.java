package com.xebialabs.overthere.ssh;

import static com.xebialabs.deployit.ci.HostAccessMethod.SSH_SUDO;
import static com.xebialabs.deployit.ci.OperatingSystemFamily.UNIX;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xebialabs.deployit.ci.Host;

public class SshSudoHostSessionItest extends SshSudoHostSessionItestBase {

	@Override
	protected void setupTargetHost() {
		targetHost = new Host();
		targetHost.setAddress("was-61");
		targetHost.setUsername("root");
		targetHost.setPassword("centos");
		targetHost.setSudoUsername("autodpl");
		targetHost.setOperatingSystemFamily(UNIX);
		targetHost.setAccessMethod(SSH_SUDO);
	}

	@Test
	public void hostSessionIsAnSshSudoHostSession() {
		assertEquals(SshSudoHostSession.class, session.getClass());
	}


}
