package com.xebialabs.overthere;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import com.xebialabs.deployit.ci.Host;
import com.xebialabs.deployit.ci.OperatingSystemFamily;
import com.xebialabs.deployit.ci.UnreachableHost;
import com.xebialabs.deployit.exception.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Map;

/**
 * Builds a connection to an unreachable host through a tunnel on the jumping station.
 */
public class TunnelledHostConnection implements HostConnection {
    private static final int DEFAULT_LOCAL_PORT = 9000;
    private static Logger logger = LoggerFactory.getLogger(TunnelledHostConnection.class);

    private final int tunnelPort;
    private Session tunnel;
    private final HostConnection tunnelledHostConnection;

    public TunnelledHostConnection(UnreachableHost host) {
        tunnelPort = startTunnel(host);
        tunnelledHostConnection = BrokenHostSessionFactory.getHostSession(host.getOperatingSystemFamily(), host.getAccessMethod(),
        		Host.getLocalHost().getAddress(), tunnelPort, host.getUsername(), host.getPassword(), host.getSudoUsername(), 
        		host.getTemporaryDirectoryLocation());
    }

    private int startTunnel(final UnreachableHost host) throws RuntimeIOException {
        final int localPort = findAvailablePort(DEFAULT_LOCAL_PORT);
        final Host jumpingStation = host.getJumpingStation();

        JSch jsch = new JSch();
        try {
        	tunnel = jsch.getSession(jumpingStation.getUsername(), jumpingStation.getAddress());
        	tunnel.setPassword(jumpingStation.getPassword());
        	tunnel.setUserInfo(new UserInfo() {
        		public String getPassphrase() {
        			return null;
        		}
        		
        		public String getPassword() {
        			return jumpingStation.getPassword();
        		}
        		
        		public boolean promptPassword(String s) {
        			return true;
        		}
        		
        		public boolean promptPassphrase(String s) {
        			return true;
        		}
        		
        		public boolean promptYesNo(String s) {
        			return true;
        		}
        		
        		public void showMessage(String s) {
        		}
        	});
            tunnel.connect();
            /*
             * FIXME: this assumes 
             * 1) that the target machine address does not contain a port spec
             * 2) that the connection method of the target machine is SSH
             * 3) that the SSH port is the default, i.e. 22
             */
            tunnel.setPortForwardingL(localPort, host.getAddress(), 22);
            logger.info(String.format("Started SSH tunnel on port %d via %s to %s", localPort, 
            		jumpingStation.getAddress(), host.getAddress()));
            return localPort;
        } catch (JSchException e) {
            try {
                close();
            } catch (Exception ex) {
                logger.error("Error closing SSH tunnel", ex);
            }

            throw new RuntimeIOException("Unable to start SSH tunneling", e);
        }

    }

    private static int findAvailablePort(int startingPort) {
    	int port = startingPort;
    	while (!available(port))  {
    		port++;    	
    	}
    	return port;
    }
    
    private static boolean available(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        	// do nothing
        } finally {
            if (ds != null) {
                ds.close();
            }

            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                    throw new RuntimeIOException("Unable to find an available local port for a tunnel", e);
                }
            }
        }
        return false;
    }


    public void close() {
    	if (tunnelledHostConnection != null) {
    		tunnelledHostConnection.close();
    	}
    	if (tunnel != null) {
    		tunnel.disconnect();
    		logger.info(String.format("Tunnel on port %d via %s closed", tunnelPort, tunnel.getHost()));
    	}
    }

    public OperatingSystemFamily getHostOperatingSystem() {
        return tunnelledHostConnection.getHostOperatingSystem();
    }

    public HostFile getFile(String hostPath) throws RuntimeIOException {
        return tunnelledHostConnection.getFile(hostPath);
    }

    public HostFile getFile(HostFile parent, String child) throws RuntimeIOException {
        return tunnelledHostConnection.getFile(parent, child);
    }

    public HostFile getTempFile(String nameTemplate) throws RuntimeIOException {
        return tunnelledHostConnection.getTempFile(nameTemplate);
    }

    public HostFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
        return tunnelledHostConnection.getTempFile(prefix, suffix);
    }

    public int execute(CommandExecutionCallbackHandler handler, String... commandLine) throws RuntimeIOException {
        return tunnelledHostConnection.execute(handler, commandLine);
    }

    public int execute(CommandExecutionCallbackHandler handler, Map<String, String> inputResponse, String... commandLine) {
        return tunnelledHostConnection.execute(handler, inputResponse, commandLine);
    }

    public CommandExecution startExecute(String... commandLine) {
        return tunnelledHostConnection.startExecute(commandLine);
    }

    public HostFile copyToTemporaryFile(File localFile) throws RuntimeIOException {
        return tunnelledHostConnection.copyToTemporaryFile(localFile);
    }

    public HostFile copyToTemporaryFile(Resource resource) throws RuntimeIOException {
        return tunnelledHostConnection.copyToTemporaryFile(resource);
    }
}
