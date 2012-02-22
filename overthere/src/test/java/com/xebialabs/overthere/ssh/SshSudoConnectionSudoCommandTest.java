/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.ssh;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_COMMAND_PREFIX;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_QUOTE_COMMAND;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SUDO_USERNAME;
import static com.xebialabs.overthere.ssh.SshConnectionType.SUDO;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.xebialabs.overthere.util.DefaultAddressPortResolver;
import org.junit.Before;
import org.junit.Test;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.ConnectionOptions;

public class SshSudoConnectionSudoCommandTest {

	private ConnectionOptions connectionOptions;
	private DefaultAddressPortResolver resolver;

	@Before
	public void init() {
		connectionOptions = new ConnectionOptions();
		connectionOptions.set(CONNECTION_TYPE, SUDO);
		connectionOptions.set(OPERATING_SYSTEM, UNIX);
		connectionOptions.set(ADDRESS, "nowhere.example.com");
		connectionOptions.set(USERNAME, "some-user");
		connectionOptions.set(PASSWORD, "foo");
		connectionOptions.set(SUDO_USERNAME, "some-other-user");
		resolver = new DefaultAddressPortResolver();
	}

	@Test
	public void shouldUseDefaultSudoCommandPrefixIfNotConfiguredExplicitly() {
		SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);
		
		List<CmdLineArgument> args = connection.processCommandLine(CmdLine.build("ls", "/tmp")).getArguments();
		assertThat(args.size(), equalTo(5));
		assertThat(args.get(0).toString(), equalTo("sudo"));
		assertThat(args.get(1).toString(), equalTo("-u"));
		assertThat(args.get(2).toString(), equalTo("some-other-user"));
		assertThat(args.get(3).toString(), equalTo("ls"));
		assertThat(args.get(4).toString(), equalTo("/tmp"));
	}

	@Test
	public void shouldUseConfiguredSudoCommandPrefix() {
		connectionOptions.set(SUDO_COMMAND_PREFIX, "sx -u {0}");
		SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);
		
		List<CmdLineArgument> args = connection.processCommandLine(CmdLine.build("ls", "/tmp")).getArguments();
		assertThat(args.size(), equalTo(5));
		assertThat(args.get(0).toString(), equalTo("sx"));
		assertThat(args.get(1).toString(), equalTo("-u"));
		assertThat(args.get(2).toString(), equalTo("some-other-user"));
		assertThat(args.get(3).toString(), equalTo("ls"));
		assertThat(args.get(4).toString(), equalTo("/tmp"));
	}

	@Test
	public void shouldUseConfiguredSudoCommandPrefixWithoutCurlyZero() {
		connectionOptions.set(SUDO_COMMAND_PREFIX, "sx");
		SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

		List<CmdLineArgument> args = connection.processCommandLine(CmdLine.build("ls", "/tmp")).getArguments();
		assertThat(args.size(), equalTo(3));
		assertThat(args.get(0).toString(), equalTo("sx"));
		assertThat(args.get(1).toString(), equalTo("ls"));
		assertThat(args.get(2).toString(), equalTo("/tmp"));
	}

	@Test
	public void shouldQuoteOriginalCommand() {
		connectionOptions.set(SUDO_COMMAND_PREFIX, "su -u {0}");
		connectionOptions.set(SUDO_QUOTE_COMMAND, true);
		SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);
		
		CmdLine cmdLine = connection.processCommandLine(CmdLine.build("ls", "/tmp"));
		List<CmdLineArgument> args = cmdLine.getArguments();
		assertThat(args.size(), equalTo(4));
		assertThat(args.get(0).toString(), equalTo("su"));
		assertThat(args.get(1).toString(), equalTo("-u"));
		assertThat(args.get(2).toString(), equalTo("some-other-user"));
		assertThat(args.get(3).toString(), equalTo("ls\\ /tmp"));
		assertThat(cmdLine.toString(), equalTo("su -u some-other-user ls\\ /tmp"));
	}

	@Test
	public void commandWithPipeShouldHaveTwoSudoSectionsIfNotQuotingCommand() {
		SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

		CmdLine cmdLine = new CmdLine().addArgument("a").addRaw("|").addArgument("b");
		List<CmdLineArgument> prefixed = connection.prefixWithSudoCommand(cmdLine).getArguments();
		assertThat(prefixed.size(), equalTo(9));
		assertThat(prefixed.get(0).toString(), equalTo("sudo"));
		assertThat(prefixed.get(5).toString(), equalTo("sudo"));
	}

	@Test
	public void commandWithPipeShouldNotHaveTwoSudoSectionsIfQuotingCommand() {
		connectionOptions.set(SUDO_COMMAND_PREFIX, "su -u {0}");
		connectionOptions.set(SUDO_QUOTE_COMMAND, true);
		SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

		CmdLine cmdLine = new CmdLine().addArgument("a").addRaw("|").addArgument("b");
		List<CmdLineArgument> prefixed = connection.prefixWithSudoCommand(cmdLine).getArguments();
		assertThat(prefixed.size(), equalTo(4));
		assertThat(prefixed.get(0).toString(), equalTo("su"));
		assertThat(prefixed.get(1).toString(), equalTo("-u"));
		assertThat(prefixed.get(2).toString(), equalTo("some-other-user"));
		assertThat(prefixed.get(3).toString(), equalTo("a\\ \\|\\ b"));
	}

	@Test
	public void commandWithSemiColonShouldHaveTwoSudoSectionsIfNotQuotingCommand() {
		SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

		CmdLine cmdLine = new CmdLine().addArgument("a").addRaw(";").addArgument("b");
		List<CmdLineArgument> prefixed = connection.prefixWithSudoCommand(cmdLine).getArguments();
		assertThat(prefixed.size(), equalTo(9));
		assertThat(prefixed.get(0).toString(), equalTo("sudo"));
		assertThat(prefixed.get(5).toString(), equalTo("sudo"));
	}

	@Test
	public void commandWithSemiColonShouldNotHaveTwoSudoSectionsIfQuotingCommand() {
		connectionOptions.set(SUDO_COMMAND_PREFIX, "su -u {0}");
		connectionOptions.set(SUDO_QUOTE_COMMAND, true);
		SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

		CmdLine cmdLine = new CmdLine().addArgument("a").addRaw(";").addArgument("b");
		List<CmdLineArgument> prefixed = connection.prefixWithSudoCommand(cmdLine).getArguments();
		assertThat(prefixed.size(), equalTo(4));
		assertThat(prefixed.get(0).toString(), equalTo("su"));
		assertThat(prefixed.get(1).toString(), equalTo("-u"));
		assertThat(prefixed.get(2).toString(), equalTo("some-other-user"));
		assertThat(prefixed.get(3).toString(), equalTo("a\\ \\;\\ b"));
	}

}

