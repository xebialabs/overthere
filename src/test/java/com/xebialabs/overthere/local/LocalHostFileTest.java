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

package com.xebialabs.overthere.local;

import com.xebialabs.deployit.ci.Host;
import com.xebialabs.overthere.CapturingCommandExecutionCallbackHandler;
import com.xebialabs.overthere.DebugCommandExecutionCallbackHandler;
import com.xebialabs.overthere.HostFile;
import com.xebialabs.overthere.HostSession;
import com.xebialabs.overthere.HostSessionItestBase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LocalHostFileTest extends HostSessionItestBase {

	private HostSession session;

	@Before
	public void openSession() {
		session = Host.getLocalHost().getHostSession();
	}

	@Test
	public void isDirectoryWorks() {
		HostFile tempFile = session.getTempFile("tmpDir");
		tempFile.delete();
		tempFile.mkdir();
		assertTrue("expected temp is a dir", tempFile.isDirectory());
	}

	@Test
	public void passwordNotSeen() {
			CapturingCommandExecutionCallbackHandler handler = new CapturingCommandExecutionCallbackHandler(); 
			session.execute(handler, "foo.sh -username","benoit","-password","benoit");
			assertThat(handler.getOutput(), )
	}

	@Test
	public void passwordNotSeen2() {
		try {
			session.execute(new DebugCommandExecutionCallbackHandler(), "foo.sh -username benoit -password benoit");
		} catch (Throwable t) {

		}
	}

	@After
	public void closeSession() {
		session.close();
	}

}
