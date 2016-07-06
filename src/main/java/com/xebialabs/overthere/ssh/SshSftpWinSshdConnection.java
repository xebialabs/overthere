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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;

import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static java.lang.Character.toUpperCase;
import static java.lang.String.format;

/**
 * A connection to a Windows host running WinSSHD.
 */
class SshSftpWinSshdConnection extends SshSftpConnection {

    public SshSftpWinSshdConnection(String type, ConnectionOptions options, AddressPortMapper mapper) {
        super(type, options, mapper);
        checkArgument(os == WINDOWS, "Cannot create a %s connection to a host that is not running Windows", protocolAndConnectionType);
    }

    @Override
    protected void connect() {
        // In WinSSHd we have to wait for the server ident before sending our ident.
//        config.setWaitForServerIdentBeforeSendingClientIdent(true);
        super.connect();
    }

    @Override
    protected String pathToSftpPath(String path) {
        String translatedPath;
        if (path.length() >= 2 && path.charAt(1) == ':') {
            char driveLetter = toUpperCase(path.charAt(0));
            String pathInDrive = path.substring(2).replace('\\', '/');
            translatedPath = "/" + driveLetter + pathInDrive;
        } else {
            throw new RuntimeIOException(format("Cannot translate Windows path [%s] to a WinSSHD path because it is not a Windows path", path));
        }
        logger.trace("Translated Windows path [{}] to WinSSHD path [{}]", path, translatedPath);
        return translatedPath;
    }

    private static Logger logger = LoggerFactory.getLogger(SshSftpWinSshdConnection.class);

}
