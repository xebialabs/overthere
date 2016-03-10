/**
 * Copyright (c) 2008-2015, XebiaLabs B.V., All rights reserved.
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
package com.xebialabs.overthere.proxy;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

import javax.net.SocketFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static java.net.InetSocketAddress.createUnresolved;

/**
 * Transparent connection that ensures that a correct SocketFactory is introduced that connects through the required
 * proxy host.
 */
@Protocol(name = "proxy")
public class ProxyTunnelConnection extends BaseOverthereConnection implements AddressPortMapper, OverthereConnectionBuilder {
    public static final int DEFAULT_PROXY_PORT = 8080;

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#proxyType">the online documentation</a>
     */
    public static final String PROXY_TYPE = "proxyType";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#proxyType">the online documentation</a>
     */
    public static final Proxy.Type PROXY_TYPE_DEFAULT = Proxy.Type.HTTP;

    private final String proxyAddress;
    private final int proxyPort;
    private final Proxy.Type proxyType;

    protected ProxyTunnelConnection(String protocol, ConnectionOptions options, AddressPortMapper mapper) {
        super(protocol, options, mapper, false);
        String unmappedAddress = options.get(ADDRESS);
        int unmappedPort = options.getInteger(PORT, DEFAULT_PROXY_PORT);
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        proxyAddress = addressPort.getHostName();
        proxyPort = addressPort.getPort();
        proxyType = options.getEnum(PROXY_TYPE, Proxy.Type.class, PROXY_TYPE_DEFAULT);
    }

    @Override
    public InetSocketAddress map(InetSocketAddress address) {
        return mapper.map(address);
    }

    @Override
    public SocketFactory socketFactory() {
        Proxy p = new Proxy(proxyType, new InetSocketAddress(proxyAddress, proxyPort));
        return new ProxySocketFactory(p);
    }

    @Override
    protected void doClose() {
        // no-op
    }

    @Override
    protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
        throw new UnsupportedOperationException("Cannot get a file from the tunnel.");
    }

    @Override
    public OverthereFile getFile(String hostPath) throws RuntimeIOException {
        throw new UnsupportedOperationException("Cannot get a file from the tunnel.");
    }

    @Override
    public OverthereFile getFile(OverthereFile parent, String child) {
        throw new UnsupportedOperationException("Cannot get a file from the tunnel.");
    }

    @Override
    public OverthereProcess startProcess(CmdLine commandLine) {
        throw new UnsupportedOperationException("Cannot start a process on the tunnel.");
    }

    @Override
    public void setWorkingDirectory(OverthereFile workingDirectory) {
        throw new UnsupportedOperationException("Cannot set a working directory on the tunnel.");
    }

    @Override
    public OverthereFile getWorkingDirectory() {
        throw new UnsupportedOperationException("Cannot get a working directory from the tunnel.");
    }

    @Override
    public int execute(final OverthereExecutionOutputHandler stdoutHandler, final OverthereExecutionOutputHandler stderrHandler, final CmdLine commandLine) {
        throw new UnsupportedOperationException("Cannot execute a command on the tunnel.");
    }

    @Override
    public String toString() {
        return "proxy:";
    }

    @Override
    public OverthereConnection connect() {
        return this;
    }
}
