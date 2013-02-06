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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.ConnectionOptions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SshSudoConnectionSudoCommandTest {

    private ConnectionOptions connectionOptions;
    private DefaultAddressPortMapper resolver;

    @BeforeMethod
    public void init() {
        connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SUDO);
        connectionOptions.set(OPERATING_SYSTEM, UNIX);
        connectionOptions.set(ADDRESS, "nowhere.example.com");
        connectionOptions.set(USERNAME, "some-user");
        connectionOptions.set(PASSWORD, "foo");
        connectionOptions.set(SUDO_USERNAME, "some-other-user");
        resolver = new DefaultAddressPortMapper();
    }

    @Test
    public void shouldUseDefaultSudoCommandPrefixIfNotConfiguredExplicitly() {
        @SuppressWarnings("resource")
        SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

        List<CmdLineArgument> args = connection.processCommandLine(CmdLine.build("ls", "/tmp")).getArguments();
        assertThat(args.size(), equalTo(5));
        assertThat(args.get(0).toString(UNIX, false), equalTo("sudo"));
        assertThat(args.get(1).toString(UNIX, false), equalTo("-u"));
        assertThat(args.get(2).toString(UNIX, false), equalTo("some-other-user"));
        assertThat(args.get(3).toString(UNIX, false), equalTo("ls"));
        assertThat(args.get(4).toString(UNIX, false), equalTo("/tmp"));
    }

    @Test
    public void shouldUseConfiguredSudoCommandPrefix() {
        connectionOptions.set(SUDO_COMMAND_PREFIX, "sx -u {0}");
        @SuppressWarnings("resource")
        SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

        List<CmdLineArgument> args = connection.processCommandLine(CmdLine.build("ls", "/tmp")).getArguments();
        assertThat(args.size(), equalTo(5));
        assertThat(args.get(0).toString(UNIX, false), equalTo("sx"));
        assertThat(args.get(1).toString(UNIX, false), equalTo("-u"));
        assertThat(args.get(2).toString(UNIX, false), equalTo("some-other-user"));
        assertThat(args.get(3).toString(UNIX, false), equalTo("ls"));
        assertThat(args.get(4).toString(UNIX, false), equalTo("/tmp"));
    }

    @Test
    public void shouldUseConfiguredSudoCommandPrefixWithoutCurlyZero() {
        connectionOptions.set(SUDO_COMMAND_PREFIX, "sx");
        @SuppressWarnings("resource")
        SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

        List<CmdLineArgument> args = connection.processCommandLine(CmdLine.build("ls", "/tmp")).getArguments();
        assertThat(args.size(), equalTo(3));
        assertThat(args.get(0).toString(UNIX, false), equalTo("sx"));
        assertThat(args.get(1).toString(UNIX, false), equalTo("ls"));
        assertThat(args.get(2).toString(UNIX, false), equalTo("/tmp"));
    }

    @Test
    public void shouldQuoteOriginalCommand() {
        connectionOptions.set(SUDO_COMMAND_PREFIX, "su -u {0}");
        connectionOptions.set(SUDO_QUOTE_COMMAND, true);
        @SuppressWarnings("resource")
        SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

        CmdLine cmdLine = connection.processCommandLine(CmdLine.build("ls", "/tmp"));
        List<CmdLineArgument> args = cmdLine.getArguments();
        assertThat(args.size(), equalTo(4));
        assertThat(args.get(0).toString(UNIX, false), equalTo("su"));
        assertThat(args.get(1).toString(UNIX, false), equalTo("-u"));
        assertThat(args.get(2).toString(UNIX, false), equalTo("some-other-user"));
        assertThat(args.get(3).toString(UNIX, false), equalTo("ls\\ /tmp"));
        assertThat(cmdLine.toString(), equalTo("su -u some-other-user ls\\ /tmp"));
    }

    @Test
    public void commandWithPipeShouldHaveTwoSudoSectionsIfNotQuotingCommand() {
        @SuppressWarnings("resource")
        SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

        CmdLine cmdLine = new CmdLine().addArgument("a").addRaw("|").addArgument("b");
        List<CmdLineArgument> prefixed = connection.prefixWithSudoCommand(cmdLine).getArguments();
        assertThat(prefixed.size(), equalTo(9));
        assertThat(prefixed.get(0).toString(UNIX, false), equalTo("sudo"));
        assertThat(prefixed.get(5).toString(UNIX, false), equalTo("sudo"));
    }

    @Test
    public void commandWithPipeShouldNotHaveTwoSudoSectionsIfQuotingCommand() {
        connectionOptions.set(SUDO_COMMAND_PREFIX, "su -u {0}");
        connectionOptions.set(SUDO_QUOTE_COMMAND, true);
        @SuppressWarnings("resource")
        SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

        CmdLine cmdLine = new CmdLine().addArgument("a").addRaw("|").addArgument("b");
        List<CmdLineArgument> prefixed = connection.prefixWithSudoCommand(cmdLine).getArguments();
        assertThat(prefixed.size(), equalTo(4));
        assertThat(prefixed.get(0).toString(UNIX, false), equalTo("su"));
        assertThat(prefixed.get(1).toString(UNIX, false), equalTo("-u"));
        assertThat(prefixed.get(2).toString(UNIX, false), equalTo("some-other-user"));
        assertThat(prefixed.get(3).toString(UNIX, false), equalTo("a\\ \\|\\ b"));
    }

    @Test
    public void commandWithSemiColonShouldHaveTwoSudoSectionsIfNotQuotingCommand() {
        @SuppressWarnings("resource")
        SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

        CmdLine cmdLine = new CmdLine().addArgument("a").addRaw(";").addArgument("b");
        List<CmdLineArgument> prefixed = connection.prefixWithSudoCommand(cmdLine).getArguments();
        assertThat(prefixed.size(), equalTo(9));
        assertThat(prefixed.get(0).toString(UNIX, false), equalTo("sudo"));
        assertThat(prefixed.get(5).toString(UNIX, false), equalTo("sudo"));
    }

    @Test
    public void commandWithSemiColonShouldNotHaveTwoSudoSectionsIfQuotingCommand() {
        connectionOptions.set(SUDO_COMMAND_PREFIX, "su -u {0}");
        connectionOptions.set(SUDO_QUOTE_COMMAND, true);
        @SuppressWarnings("resource")
        SshSudoConnection connection = new SshSudoConnection(SSH_PROTOCOL, connectionOptions, resolver);

        CmdLine cmdLine = new CmdLine().addArgument("a").addRaw(";").addArgument("b");
        List<CmdLineArgument> prefixed = connection.prefixWithSudoCommand(cmdLine).getArguments();
        assertThat(prefixed.size(), equalTo(4));
        assertThat(prefixed.get(0).toString(UNIX, false), equalTo("su"));
        assertThat(prefixed.get(1).toString(UNIX, false), equalTo("-u"));
        assertThat(prefixed.get(2).toString(UNIX, false), equalTo("some-other-user"));
        assertThat(prefixed.get(3).toString(UNIX, false), equalTo("a\\ \\;\\ b"));
    }

}
