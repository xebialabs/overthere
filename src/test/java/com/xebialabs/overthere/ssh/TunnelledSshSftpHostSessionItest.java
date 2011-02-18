/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

package com.xebialabs.overthere.ssh;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.xebialabs.deployit.ci.Host;
import com.xebialabs.deployit.ci.HostAccessMethod;
import com.xebialabs.deployit.ci.OperatingSystemFamily;
import com.xebialabs.deployit.ci.UnreachableHost;
import com.xebialabs.overthere.TunnelledHostSession;

public class TunnelledSshSftpHostSessionItest extends SshHostSessionItestBase {

	@Override
	public void setupTargetHost() {
		targetHost = new UnreachableHost();
		targetHost.setLabel("Itest Host for Tunnelled SSH/SFTP access");
		targetHost.setAddress("apache-22");
		targetHost.setUsername("root");
		targetHost.setPassword("centos");
		targetHost.setOperatingSystemFamily(OperatingSystemFamily.UNIX);
		targetHost.setTemporaryDirectoryLocation("/tmp");
		targetHost.setAccessMethod(HostAccessMethod.SSH_SFTP);

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
