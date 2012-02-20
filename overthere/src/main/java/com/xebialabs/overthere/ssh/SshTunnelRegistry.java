package com.xebialabs.overthere.ssh;

import static com.google.common.base.Preconditions.checkArgument;
import static com.xebialabs.overthere.ConnectionOptions.JUMPSTATION;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.TUNNEL;

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
	 * @param jumpstationOptions
	 *                  The {@link ConnectionOptions} to be used to connect to the 'jump station' and setup the port forwards.
	 * @return A connected {@link SshTunnelConnection}. 
	 */
	synchronized static SshTunnelConnection getConnectedTunnel(ConnectionOptions jumpstationOptions) {
		SshTunnelConnection sshTunnelConnection = TUNNELS.get(jumpstationOptions);
		if (sshTunnelConnection == null) {
            checkArgument(jumpstationOptions.get(CONNECTION_TYPE) == TUNNEL, "Can only setup a tunnel with %s = %s, not [%s]", CONNECTION_TYPE, TUNNEL, jumpstationOptions.get(CONNECTION_TYPE));
			sshTunnelConnection = new SshTunnelConnection(JUMPSTATION, jumpstationOptions);
			TUNNELS.put(jumpstationOptions, sshTunnelConnection);
		}
		return sshTunnelConnection;
	}

	/**
	 * Close the {@link SshTunnelConnection} belonging to the passed in {@link ConnectionOptions}.
	 * 
	 * @param jumpstationOptions
	 *                  The {@link ConnectionOptions} that belong to the to be closed {@link SshTunnelConnection}
	 */
	public synchronized static void closeTunnel(ConnectionOptions jumpstationOptions) {
		SshTunnelConnection sshTunnelConnection = TUNNELS.get(jumpstationOptions);
		if (sshTunnelConnection == null) {
			logger.error("Trying to close a tunnel which is no longer there...");
		} else {
			sshTunnelConnection.close();
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(SshTunnelRegistry.class);

}
