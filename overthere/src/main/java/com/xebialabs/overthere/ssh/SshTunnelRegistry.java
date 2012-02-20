package com.xebialabs.overthere.ssh;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.ConnectionOptions.TUNNEL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.xebialabs.overthere.ConnectionOptions;

/**
 * Registry that manages {@link SshTunnelConnection} connections. 
 */
public class SshTunnelRegistry {
	private static final Map<ConnectionOptions, SshTunnelConnection> TUNNELS = Maps.newHashMap();

	/**
	 * Creates or re-uses a pre-existing {@link SshTunnelConnection}.
	 * @param tunnelOptions
	 *                  The {@link ConnectionOptions} to be used to connect to the 'jump station' and setup the port forwards.
	 * @return A connected {@link SshTunnelConnection}. 
	 */
	synchronized static SshTunnelConnection getConnectedTunnel(ConnectionOptions tunnelOptions) {
		SshTunnelConnection sshTunnelConnection = TUNNELS.get(tunnelOptions);
		if (sshTunnelConnection == null) {
			checkConnectionType(tunnelOptions);
			sshTunnelConnection = new SshTunnelConnection(TUNNEL, tunnelOptions);
			TUNNELS.put(tunnelOptions, sshTunnelConnection);
		} else {
			// The tunnel might be inactive (ie. closed), try connecting to it.
			sshTunnelConnection.connect();
		}
		return sshTunnelConnection;
	}

	/**
	 * Close the {@link SshTunnelConnection} belonging to the passed in {@link ConnectionOptions}.
	 * 
	 * @param tunnelOptions
	 *                  The {@link ConnectionOptions} that belong to the to be closed {@link SshTunnelConnection}
	 */
	public synchronized static void closeTunnel(ConnectionOptions tunnelOptions) {
		SshTunnelConnection sshTunnelConnection = TUNNELS.get(tunnelOptions);
		if (sshTunnelConnection == null) {
			logger.error("Trying to close a tunnel which is no longer there...");
		} else {
			sshTunnelConnection.close();
		}
	}

	private static void checkConnectionType(ConnectionOptions tunnelOptions) {
		Object connectionType = tunnelOptions.get(CONNECTION_TYPE);
		checkArgument(connectionType == SshConnectionType.TUNNEL, "Can only setup a tunnel with %s = %s, not [%s]", CONNECTION_TYPE, SshConnectionType.TUNNEL, connectionType);
	}

	private static final Logger logger = LoggerFactory.getLogger(SshTunnelRegistry.class);
}
