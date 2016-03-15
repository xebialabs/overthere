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
package com.xebialabs.overthere.proxy;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.BaseOverthereConnection;
import com.xebialabs.overthere.spi.OverthereConnectionBuilder;
import com.xebialabs.overthere.spi.Protocol;

import javax.net.SocketFactory;
import java.net.InetSocketAddress;
import java.net.Proxy;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.UNIX;
import static com.xebialabs.overthere.proxy.ProxyConnection.PROXY_PROTOCOL;
import static java.net.InetSocketAddress.createUnresolved;
import static java.net.Proxy.Type.HTTP;

/**
 * Transparent connection that ensures that a correct SocketFactory is introduced that connects through the required
 * proxy host.
 */
@Protocol(name = PROXY_PROTOCOL)
public class ProxyConnection extends BaseOverthereConnection implements AddressPortMapper, OverthereConnectionBuilder {

    /**
     * Name of the protocol handled by this connection builder, i.e. "ssh".
     */
    public static final String PROXY_PROTOCOL = "proxy";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#proxyType">the online documentation</a>
     */
    public static final String PROXY_TYPE = "proxyType";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#proxyType">the online documentation</a>
     */
    public static final Proxy.Type PROXY_TYPE_DEFAULT = HTTP;

    private final String proxyAddress;
    private final int proxyPort;
    private final Proxy.Type proxyType;

    public ProxyConnection(String protocol, ConnectionOptions options, AddressPortMapper mapper) {
        super(protocol, augmentOptions(options), mapper, false);
        String unmappedAddress = options.get(ADDRESS);
        int unmappedPort = options.getInteger(PORT);
        InetSocketAddress addressPort = mapper.map(createUnresolved(unmappedAddress, unmappedPort));
        proxyAddress = addressPort.getHostName();
        proxyPort = addressPort.getPort();
        proxyType = options.getEnum(PROXY_TYPE, Proxy.Type.class, PROXY_TYPE_DEFAULT);
        if(proxyType != HTTP) {
            throw new IllegalArgumentException("Proxy of type other than HTTP not supported");
        }
        if(options.containsKey(JUMPSTATION)) {
            throw new IllegalArgumentException("Cannot configure an HTTP proxy behind another proxy or behind an SSH jumpstation");
        }
    }

    private static ConnectionOptions augmentOptions(ConnectionOptions options) {
        if (options.containsKey(OPERATING_SYSTEM)) {
            return options;
        } else {
            ConnectionOptions augmentedOptions = new ConnectionOptions(options);
            augmentedOptions.set(OPERATING_SYSTEM, UNIX);
            return augmentedOptions;
        }
    }

    @Override
    public OverthereConnection connect() {
        return this;
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
        throw new UnsupportedOperationException("Cannot get a file from the proxy.");
    }

    @Override
    public OverthereFile getFile(String hostPath) throws RuntimeIOException {
        throw new UnsupportedOperationException("Cannot get a file from the proxy.");
    }

    @Override
    public OverthereFile getFile(OverthereFile parent, String child) {
        throw new UnsupportedOperationException("Cannot get a file from the proxy.");
    }

    @Override
    public OverthereProcess startProcess(CmdLine commandLine) {
        throw new UnsupportedOperationException("Cannot start a process on the proxy.");
    }

    @Override
    public void setWorkingDirectory(OverthereFile workingDirectory) {
        throw new UnsupportedOperationException("Cannot set a working directory on the proxy.");
    }

    @Override
    public OverthereFile getWorkingDirectory() {
        throw new UnsupportedOperationException("Cannot get a working directory from the proxy.");
    }

    @Override
    public int execute(final OverthereExecutionOutputHandler stdoutHandler, final OverthereExecutionOutputHandler stderrHandler, final CmdLine commandLine) {
        throw new UnsupportedOperationException("Cannot execute a command on the proxy.");
    }

    @Override
    public String toString() {
        return "proxy:" + proxyType.toString().toLowerCase() + "://" + proxyAddress + ":" + proxyPort;
    }

}
