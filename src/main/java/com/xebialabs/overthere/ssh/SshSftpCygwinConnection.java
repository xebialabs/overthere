/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.CmdLineArgument;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;

import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_WINDOWS;
import static com.xebialabs.overthere.ConnectionOptions.FILE_COPY_COMMAND_FOR_WINDOWS_DEFAULT;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static java.lang.Character.toLowerCase;
import static java.lang.String.format;

/**
 * A connection to a Windows host running OpenSSH on Cygwin using SSH w/ SFTP.
 */
class SshSftpCygwinConnection extends SshSftpConnection {

    public SshSftpCygwinConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, fixOptions(options), mapper);
        checkArgument(os == WINDOWS, "Cannot create a %s connection to a host that is not running Windows", protocolAndConnectionType);
    }

    private static ConnectionOptions fixOptions(final ConnectionOptions options) {
        ConnectionOptions fixedOptions = new ConnectionOptions(options);
        fixedOptions.set(FILE_COPY_COMMAND_FOR_WINDOWS, "cmd /c " + fixedOptions.get(FILE_COPY_COMMAND_FOR_WINDOWS, FILE_COPY_COMMAND_FOR_WINDOWS_DEFAULT));
        return fixedOptions;
    }

    @Override
    protected String pathToSftpPath(String path) {
        String translatedPath = toCygwinPath(path);
        if (translatedPath == null) {
            throw new RuntimeIOException(format("Cannot translate Windows path [%s] to a Cygdrive path because it is not a Windows path or a Cygwin path", path));
        }
        return translatedPath;
    }

    protected String toCygwinPath(String path) {
        if (path.length() >= 2 && path.charAt(1) == ':') {
            char driveLetter = toLowerCase(path.charAt(0));
            String pathInDrive = path.substring(2).replace('\\', '/');
            String cygwinPath = "/cygdrive/" + driveLetter + pathInDrive;
            logger.trace("Translated Windows path [{}] to Cygdrive path [{}]", path, cygwinPath);
            return cygwinPath;
        } else if (path.startsWith("/cygdrive/")) {
            return path;
        } else {
            return null;
        }
    }

    @Override
    protected CmdLine processCommandLine(final CmdLine cmd) {
        List<CmdLineArgument> args = cmd.getArguments();
        checkArgument(args.size() > 0, "Empty command line");

        String arg0 = args.get(0).toString();
        String arg0CygwinPath = toCygwinPath(arg0);
        if (arg0CygwinPath != null) {
            CmdLine modifiedCommandLine = new CmdLine();
            modifiedCommandLine.add(CmdLineArgument.arg(arg0CygwinPath));
            for (int i = 1; i < args.size(); i++) {
                modifiedCommandLine.add(args.get(i));
            }
            logger.trace("Translated first element (command) of command line from Windows path [{}] to Cygwin path [{}]", arg0, arg0CygwinPath);
            return super.processCommandLine(modifiedCommandLine);
        } else {
            return super.processCommandLine(cmd);
        }
    }

    @Override
    protected SshProcess createProcess(Session session, CmdLine commandLine) throws TransportException, ConnectionException {
        return new SshProcess(this, UNIX, session, commandLine);
    }

    private static Logger logger = LoggerFactory.getLogger(SshSftpCygwinConnection.class);

}
