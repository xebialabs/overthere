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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.CmdLine;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereExecutionOutputHandler;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.OverthereProcess;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;

import javax.net.SocketFactory;

import static com.xebialabs.overthere.util.OverthereUtils.checkState;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PORT_ALLOCATION_RANGE_START;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.PORT_ALLOCATION_RANGE_START_DEFAULT;
import static com.xebialabs.overthere.util.OverthereUtils.closeQuietly;
import static java.lang.String.format;
import static java.net.InetSocketAddress.createUnresolved;

/**
 * A connection to a 'jump station' host using SSH w/ local port forwards.
 */
public class SshTunnelConnection extends SshConnection implements AddressPortMapper {

    private static final AtomicReference<TunnelPortManager> PORT_MANAGER = new AtomicReference<TunnelPortManager>(new TunnelPortManager());

    private static final int MAX_PORT = 65535;

    private Map<InetSocketAddress, InetSocketAddress> localPortForwards = new HashMap<InetSocketAddress, InetSocketAddress>();

    private List<PortForwarder> portForwarders = new ArrayList<PortForwarder>();

    private int startPortRange;

    private final ReentrantLock lock = new ReentrantLock();

    public SshTunnelConnection(final String protocol, final ConnectionOptions options, final AddressPortMapper mapper) {
        super(protocol, options, mapper);
        this.startPortRange = options.getInteger(PORT_ALLOCATION_RANGE_START, PORT_ALLOCATION_RANGE_START_DEFAULT);
    }

    @Override
    protected void connect() {
        super.connect();
        checkState(sshClient != null, "Should have set an SSH client when connected");
    }

    @Override
    public void doClose() {
        logger.debug("Closing tunnel.");
        for (PortForwarder portForwarder : portForwarders) {
            closeQuietly(portForwarder);
        }

        super.doClose();
    }

    @Override
    public InetSocketAddress map(InetSocketAddress address) {
        lock.lock();
        try {
            if (localPortForwards.containsKey(address)) {
                return localPortForwards.get(address);
            }

            ServerSocket serverSocket = PORT_MANAGER.get().bindToNextFreePort(startPortRange);
            portForwarders.add(startForwarder(address, serverSocket));

            InetSocketAddress localAddress = createUnresolved("localhost", serverSocket.getLocalPort());
            localPortForwards.put(address, localAddress);
            return localAddress;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public SocketFactory socketFactory() {
        return SocketFactory.getDefault();
    }

    private PortForwarder startForwarder(InetSocketAddress remoteAddress, ServerSocket serverSocket) {
        PortForwarder forwarderThread = new PortForwarder(sshClient, remoteAddress, serverSocket);
        logger.info("Starting {}", forwarderThread.getName());
        forwarderThread.start();
        try {
            forwarderThread.latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return forwarderThread;
    }

    @Override
    public OverthereFile getFile(String hostPath) throws RuntimeIOException {
        throw new UnsupportedOperationException("Cannot get a file from the tunnel.");
    }

    @Override
    public OverthereProcess startProcess(CmdLine commandLine) {
        throw new UnsupportedOperationException("Cannot start a process on the tunnel.");
    }

    @Override
    protected CmdLine processCommandLine(CmdLine cmd) {
        throw new UnsupportedOperationException("Cannot process a command line for the tunnel.");
    }

    @Override
    protected SshProcess createProcess(Session session, CmdLine commandLine) throws TransportException, ConnectionException {
        throw new UnsupportedOperationException("Cannot create a process in the tunnel.");
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

    private static class PortForwarder extends Thread implements Closeable {
        private final SSHClient sshClient;
        private final InetSocketAddress remoteAddress;
        private final ServerSocket localSocket;
        private CountDownLatch latch = new CountDownLatch(1);
        private LocalPortForwarder forwarder;

        public PortForwarder(SSHClient sshClient, InetSocketAddress remoteAddress, ServerSocket localSocket) {
            super(buildName(remoteAddress, localSocket.getLocalPort()));
            this.sshClient = sshClient;
            this.remoteAddress = remoteAddress;
            this.localSocket = localSocket;
        }

        private static String buildName(InetSocketAddress remoteAddress, Integer localPort) {
            return format("SSH local port forward thread %d:%s", localPort, remoteAddress.toString());
        }

        @Override
        public void run() {
            LocalPortForwarder.Parameters params = new LocalPortForwarder.Parameters("localhost", localSocket.getLocalPort(),
                    remoteAddress.getHostName(), remoteAddress.getPort());
            forwarder = sshClient.newLocalPortForwarder(params, localSocket);
            try {
                latch.countDown();
                forwarder.listen();
            } catch (IOException ignore) {
                // OK.
            }
        }

        @Override
        public void close() throws IOException {
            forwarder.close();
            localSocket.close();

            try {
                this.join();
            } catch (InterruptedException e) {
                // OK.
            }
        }
    }

    static class TunnelPortManager {
        private AtomicInteger lastBoundPort = new AtomicInteger(0);
        private ReentrantLock lock = new ReentrantLock();

        ServerSocket bindToNextFreePort(int startFrom) {
            lock.lock();
            try {
                int firstPort = Math.max(startFrom, lastBoundPort.get() + 1);
                int port = firstPort;
                for (; ; ) {
                    logger.trace("Trying to bind to port {}", port);
                    ServerSocket socket = tryBind(port);
                    if (socket != null) {
                        logger.debug("Successfully bound to port {}.", port);
                        lastBoundPort.set(port);
                        return socket;
                    }

                    if (port == MAX_PORT) {
                        port = startFrom;
                    } else {
                        port++;
                    }

                    if (port == firstPort) {
                        throw new IllegalStateException(format("Could not find a single free port in the range [%s-%s]...", startFrom, MAX_PORT));
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        protected ServerSocket tryBind(int localPort) {
            try {
                ServerSocket ss = new ServerSocket();
                ss.setReuseAddress(true);
                ss.bind(new InetSocketAddress("localhost", localPort));
                return ss;
            } catch (IOException e) {
                return null;
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SshTunnelConnection.class);

}
