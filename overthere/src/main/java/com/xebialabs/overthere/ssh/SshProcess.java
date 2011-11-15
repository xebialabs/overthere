package com.xebialabs.overthere.ssh;

import java.io.InputStream;
import java.io.OutputStream;

import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Signal;
import net.schmizz.sshj.transport.TransportException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;

class SshProcess implements OverthereProcess {
    private SshConnection connection;
    private final Session session;
    private final CmdLine commandLine;
    private final Session.Command command;

    SshProcess(final SshConnection connection, final Session session, final CmdLine commandLine) throws TransportException, ConnectionException {
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
            Integer exitStatus = command.getExitStatus();
            logger.info("Command {} on {} returned {}", new Object[] { commandLine, connection, exitStatus });
	        closeSession();
	        if(exitStatus == null) {
	        	return -1;
	        } else {
	        	return exitStatus;
	        }
        } catch (ConnectionException e) {
            throw new RuntimeIOException("Caught exception while awaiting end of process", e);
        }
    }

    @Override
    public void destroy() {
	    try {
		    command.signal(Signal.KILL);
	    } catch (TransportException e) {
		    logger.warn("Could not send the KILL signal to the command, closing the session.", e);
	    } finally {
		    closeSession();
	    }
    }

	private void closeSession() {
		if (session.isOpen()) {
		    try {
		        session.close();
		    } catch (SSHException e) {
		        throw new RuntimeIOException("Could not close the SSH session", e);
		    }
		}
	}

	private static Logger logger = LoggerFactory.getLogger(SshProcess.class);

}
