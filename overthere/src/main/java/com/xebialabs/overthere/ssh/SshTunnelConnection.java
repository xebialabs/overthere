package com.xebialabs.overthere.ssh;

import com.google.common.base.Splitter;
import com.google.common.io.Closeables;
import com.xebialabs.overthere.*;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.LOCAL_PORT_FORWARDS;
import static java.lang.String.format;

/**
 * A connection to a 'jump station' host using SSH w/ local port forwards.
 */
public class SshTunnelConnection extends SshConnection {

	private List<LocalPortForwardConfig> localPortForwards = newArrayList();
	
	private List<PortForwarder> portForwarders = newArrayList();

	private AtomicInteger referenceCounter = new AtomicInteger(0);

	public SshTunnelConnection(final String protocol, final ConnectionOptions options) {
		super(protocol, options);
		this.localPortForwards = parseLocalPortForwards(options.<String>get(LOCAL_PORT_FORWARDS));
	}

	/**
	 * Establishes the connection to the 'jump station' and sets up the local port forwards, if it hasn't already done so.
	 */
	@Override
	protected void connect() {
		if (referenceCounter.getAndIncrement() == 0) {
			super.connect();
			checkState(sshClient != null, "Should have set an SSH client when connected");
			try {
				startPortForwarders();
			} catch (Exception e) {
				Closeables.closeQuietly(this);
				throw new RuntimeIOException("Cannot setup portforwards", e);
			}
		} else {
			logger.debug("Reusing tunnel, now in use by [{}] connections", referenceCounter.get());
		}
	}

	/**
	 * Closes the {@link SshTunnelConnection} only if there are no more references to it from other connections. 
	 */
	@Override
	public void doClose() {
		if (referenceCounter.decrementAndGet() == 0) {
			logger.debug("Closing tunnel.");
			for (PortForwarder portForwarder : portForwarders) {
				Closeables.closeQuietly(portForwarder);
			}

			super.doClose();
		} else {
			logger.debug("Not closing tunnel, still in use by [{}] other connections", referenceCounter.get());
		}
	}

	/**
	 * Rewrite the {@link ConnectionOptions} for a connection that traverses through the 'jump station' this {@link SshTunnelConnection} is connected to.
	 * 
	 * <ul>
	 *     <li>Change the {@link ConnectionOptions#ADDRESS} connection option to 'localhost'.</li>
	 *     <li>Change the {@link ConnectionOptions#PORT} and {@link com.xebialabs.overthere.cifs.CifsConnectionBuilder#CIFS_PORT} connection options to the forwarded local ports</li>
	 * </ul>
	 * 
	 * <em>N.B.</em> the {@link ConnectionOptions#PORT} and {@link com.xebialabs.overthere.cifs.CifsConnectionBuilder#CIFS_PORT} should be supplied explicitly.
	 * 
	 * @param options
	 *              The {@link ConnectionOptions} that need rewriting.
	 * @return A rewritten copy of the {@link ConnectionOptions} passed in.
	 */
	public ConnectionOptions rewriteAddressAndPorts(ConnectionOptions options) {
		ConnectionOptions rewrittenOptions = new ConnectionOptions(options);
		String remoteHost = options.get(ADDRESS);
		rewrittenOptions.set(ADDRESS, "localhost");

		rewrittenOptions.set(PORT, rewritePort(remoteHost, (Integer) options.getOptional(PORT)));
		rewrittenOptions.set(CIFS_PORT, rewritePort(remoteHost, (Integer) options.getOptional(CIFS_PORT)));
		return rewrittenOptions;
	}

	private List<LocalPortForwardConfig> parseLocalPortForwards(String s) {
		List<LocalPortForwardConfig> localPortForwards = newArrayList();
		Iterable<String> forwards = Splitter.on(",").split(s);
		for (String f : forwards) {
			checkArgument(f.indexOf(":") > 0, "%s not of format <localPort>:<remoteHost>:<remotePort>,<localPort>:<remoteHost>:<remotePort>,...; but [%s]", LOCAL_PORT_FORWARDS, s);
			try {
				localPortForwards.add(new LocalPortForwardConfig(f));
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(format("%s not of format <localPort>:<remoteHost>:<remotePort>,<localPort>:<remoteHost>:<remotePort>,...; but [%s]", LOCAL_PORT_FORWARDS, s), nfe);
			}
		}
		return localPortForwards;
	}

	private void startPortForwarders() throws IOException {
		for (LocalPortForwardConfig portForward : localPortForwards) {
			portForwarders.add(startForwarder(portForward));
		}
	}

	private PortForwarder startForwarder(LocalPortForwardConfig portForward) throws IOException {
		PortForwarder forwarderThread = new PortForwarder(sshClient, portForward);
		logger.info("Starting {}", forwarderThread.getName());
		forwarderThread.start();
		try {
			forwarderThread.latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return forwarderThread;
	}

	private Integer rewritePort(String remoteHost, Integer port) {
		if (port == null) {
			return null;
		}

		for (LocalPortForwardConfig portForward : localPortForwards) {
			if (portForward.remoteHost.equals(remoteHost) && portForward.remotePort == port) {
				return portForward.localPort;
			}
		}
		return port;
	}

	
	@Override
	protected OverthereFile getFile(String hostPath, boolean isTempFile) throws RuntimeIOException {
		throw new UnsupportedOperationException("Cannot get a file from the tunnel.");
	}

	@Override
	protected OverthereFile getFile(OverthereFile parent, String child, boolean isTempFile) throws RuntimeIOException {
		throw new UnsupportedOperationException("Cannot get a file from the tunnel.");
	}

	@Override
	public OverthereProcess startProcess(CmdLine commandLine) {
		throw new UnsupportedOperationException("Cannot start a process on the tunnel.");
	}

	@Override
	protected CmdLine processCommandLine(CmdLine commandLine) {
		throw new UnsupportedOperationException("Cannot process a command line for the tunnel.");
	}

	@Override
	protected void addCommandSeparator(CmdLine commandLine) {
		throw new UnsupportedOperationException("Cannot add a separator to the command line in the tunnel.");
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
	public int execute(OverthereProcessOutputHandler handler, CmdLine commandLine) {
		throw new UnsupportedOperationException("Cannot execute a command on the tunnel.");
	}

	private static class LocalPortForwardConfig {
		private int localPort;
		private String remoteHost;
		private int remotePort;

		private LocalPortForwardConfig(String forward) {
			String[] f = forward.split(":");
			checkArgument(f.length == 3, " Element [%s] of %s is not of format <localPort>:<remoteHost>:<remotePort>", forward, LOCAL_PORT_FORWARDS);
			localPort = Integer.parseInt(f[0]);
			remoteHost = f[1];
			remotePort = Integer.parseInt(f[2]);
		}

		@Override
		public String toString() {
			return format("%d:%s:%d", localPort, remoteHost, remotePort);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(SshTunnelConnection.class);

	private static class PortForwarder extends Thread implements Closeable {
		private final SSHClient sshClient;
		private final LocalPortForwardConfig portForward;
		private ServerSocket ss;
		private CountDownLatch latch = new CountDownLatch(1);

		public PortForwarder(SSHClient sshClient, LocalPortForwardConfig portForward) {
			super(buildName(portForward));
			this.sshClient = sshClient;
			this.portForward = portForward;
		}

		private static String buildName(LocalPortForwardConfig portForward) {
			return format("SSH local port forward thread [%s]", portForward);
		}

		@Override
		public void run() {
			LocalPortForwarder.Parameters params = new LocalPortForwarder.Parameters("localhost", portForward.localPort, portForward.remoteHost, portForward.remotePort);
			try {
				ss = new ServerSocket();
				ss.setReuseAddress(true);
				ss.bind(new InetSocketAddress(params.getLocalHost(), params.getLocalPort()));

				LocalPortForwarder forwarder = sshClient.newLocalPortForwarder(params, ss);
				try {
					latch.countDown();
					forwarder.listen();
				} catch (IOException ignore) {
					// OK.
				}
			} catch (IOException ioe) {
				logger.error(format("Couldn't setup local port forward [%s]", portForward), ioe);
			}
		}
		
		private static final Logger logger = LoggerFactory.getLogger(PortForwarder.class);

		@Override
		public void close() throws IOException {
			ss.close();
			try {
				this.join();
			} catch (InterruptedException e) {
				// OK.
			}
		}
	}
}
