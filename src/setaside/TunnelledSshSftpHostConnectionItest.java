package com.xebialabs.overthere.ssh;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xebialabs.deployit.ci.Host;
import com.xebialabs.deployit.ci.HostAccessMethod;
import com.xebialabs.deployit.ci.OperatingSystemFamily;
import com.xebialabs.deployit.ci.UnreachableHost;
import com.xebialabs.overthere.TunnelledHostSession;

public class TunnelledSshSftpHostConnectionItest extends SshHostConnectionItestBase {

	@Override
	public void setupConnection() {
		targetHost = new UnreachableHost();
		options.set("Label", "Itest Host for Tunnelled SSH/SFTP access");
		options.set("Address", "apache-22");
		options.set("Username", "root");
		options.set("Password", "centos");
		options.set("OperatingSystemFamily", OperatingSystemFamily.UNIX);
		options.set("TemporaryDirectoryLocation", "/tmp");
		options.set("AccessMethod", HostAccessMethod.SSH_SFTP);

		Host jumpingHost = new Host();
		jumpingHost.setLabel("Jumping station for Tunnelled Itest");
		jumpingHost.setAddress("jboss-51");
		jumpingHost.setUsername("autodpl");
		jumpingHost.setPassword("autodpl");
		jumpingHost.setOperatingSystemFamily(OperatingSystemFamily.UNIX);
		jumpingHost.setAccessMethod(HostAccessMethod.SSH_SFTP);

		((UnreachableHost) targetHost).setJumpingStation(jumpingHost);
	}

	@Test
	public void hostSessionIsATunnelledHostSession() {
		assertEquals(TunnelledHostSession.class, session.getClass());
	}

}
