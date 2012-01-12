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

import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_DEFAULT_PTY;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import net.schmizz.sshj.common.SSHException;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Signal;
import net.schmizz.sshj.transport.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

class SshProcess implements OverthereProcess {
    private SshConnection connection;
    private final Session session;
    private final String encodedCommandLine;
    private final Session.Command command;

    SshProcess(final SshConnection connection, final Session session, final CmdLine commandLine) throws TransportException, ConnectionException {
        this.connection = connection;
        this.session = session;
        this.encodedCommandLine = commandLine.toCommandLine(connection.getHostOperatingSystem(), true);
        logger.debug("Executing command {} on {}", encodedCommandLine, connection);
		this.command = session.exec(commandLine.toCommandLine(connection.getHostOperatingSystem(), false));
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
            logger.info("Command {} on {} returned {}", new Object[] { encodedCommandLine, connection, exitStatus });
	        closeSession();
	        if(exitStatus == null) {
				logger.warn("Command {} on {} could not be started. This may be caused by the connection option " + ALLOCATE_DEFAULT_PTY + " being set to true.", new Object[] {
				        encodedCommandLine, connection });
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

