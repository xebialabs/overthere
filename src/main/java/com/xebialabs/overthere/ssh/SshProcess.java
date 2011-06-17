package com.xebialabs.overthere.ssh;

import java.io.InputStream;
import java.io.OutputStream;

import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;

class SshProcess implements OverthereProcess {
    private SshOverthereConnection connection;
    private final Session session;
    private final CmdLine commandLine;
    private final Session.Command command;

    SshProcess(final SshOverthereConnection connection, final Session session, final CmdLine commandLine) throws TransportException, ConnectionException {
        this.connection = connection;
        this.session = session;
        this.commandLine = commandLine;
        this.command = startCommand();
    }

    protected Session.Command startCommand() throws TransportException, ConnectionException {
        return session.exec(commandLine.toCommandLine(connection.getHostOperatingSystem(), false));
    }

    @Override
    public OutputStream getStdin() {
        return command.getOutputStream();
    }

    @Override
    public InputStream getStdout() {
        return command.getInputStream();
    }

    @Override
    public InputStream getStderr() {
        return command.getErrorStream();
    }

    @Override
    public int waitFor() {
        try {
            command.join();
            return command.getExitStatus();
        } catch (ConnectionException e) {
            throw new RuntimeIOException("Caught exception while awaiting end of process", e);
        }
    }

    @Override
    public void destroy() {
        if (session.isOpen()) {
            try {
                session.close();
            } catch (SSHException e) {
                throw new RuntimeIOException("Could not close the SSH session", e);
            }
        }
    }

}
