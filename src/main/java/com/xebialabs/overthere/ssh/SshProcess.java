/**
 * Copyright (c) 2008, 2012, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
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
import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;

class SshProcess implements OverthereProcess {
    private SshConnection connection;
    private final Session session;
    private final String encodedCommandLine;
    private final Session.Command command;

    SshProcess(final SshConnection connection, final OperatingSystemFamily os, final Session session, final CmdLine commandLine) throws TransportException,
        ConnectionException {
        this.connection = connection;
        this.session = session;
        this.encodedCommandLine = commandLine.toCommandLine(os, true);
        logger.debug("Executing command {} on {}", encodedCommandLine, connection);
        this.command = session.exec(commandLine.toCommandLine(os, false));
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
            if (exitStatus == null) {
                logger.warn("Command {} on {} could not be started. Returning exit code -1", encodedCommandLine, connection);
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
