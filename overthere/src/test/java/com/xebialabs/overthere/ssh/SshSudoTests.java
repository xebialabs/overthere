package com.xebialabs.overthere.ssh;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import java.util.List;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.OverthereConnection;

/**
 * These tests have been moved to a class in this package so that they can access package private classes and methods.
 */
public class SshSudoTests {

	public static void commandWithPipeShouldHaveTwoSudoSections(OverthereConnection connection) {
		assumeThat(connection, instanceOf(SshSudoConnection.class));

		List<CmdLineArgument> prepended = ((SshSudoConnection) connection).prefixWithSudoCommand(CmdLine.build("a", "|", "b")).getArguments();
		assertThat(prepended.size(), equalTo(9));
		assertThat(prepended.get(0).toString(false), equalTo("sudo"));
		assertThat(prepended.get(5).toString(false), equalTo("sudo"));
	}

	public static void commandWithSemiColonShouldHaveTwoSudoSections(OverthereConnection connection) {
		assumeThat(connection, instanceOf(SshSudoConnection.class));

		List<CmdLineArgument> prepended = ((SshSudoConnection) connection).prefixWithSudoCommand(CmdLine.build("a", ";", "b")).getArguments();
		assertThat(prepended.size(), equalTo(9));
		assertThat(prepended.get(0).toString(false), equalTo("sudo"));
		assertThat(prepended.get(5).toString(false), equalTo("sudo"));
	}

}
