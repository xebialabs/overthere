package com.xebialabs.overthere.ssh;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public abstract class SshSudoHostConnectionItestBase extends SshHostConnectionItestBase {

	@Test
	public void commandWithPipeShouldHaveTwoSudoSections() {
		String[] prepended = ((SshSudoHostConnection) connection).prependSudoCommand("a", "|", "b");
		assertEquals(9, prepended.length);
		assertEquals("sudo", prepended[0]);
		assertEquals("sudo", prepended[5]);
	}

	@Test
	public void commandWithSemiColonShouldHaveTwoSudoSections() {
		String[] prepended = ((SshSudoHostConnection) connection).prependSudoCommand("a", ";", "b");
		assertEquals(9, prepended.length);
		assertEquals("sudo", prepended[0]);
		assertEquals("sudo", prepended[5]);
	}

}
