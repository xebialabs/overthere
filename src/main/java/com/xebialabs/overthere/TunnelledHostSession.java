/*
 * Copyright (c) 2008-2010 XebiaLabs B.V. All rights reserved.
 *
 * Your use of XebiaLabs Software and Documentation is subject to the Personal
 * License Agreement.
 *
 * http://www.xebialabs.com/deployit-personal-edition-license-agreement
 *
 * You are granted a personal license (i) to use the Software for your own
 * personal purposes which may be used in a production environment and/or (ii)
 * to use the Documentation to develop your own plugins to the Software.
 * "Documentation" means the how to's and instructions (instruction videos)
 * provided with the Software and/or available on the XebiaLabs website or other
 * websites as well as the provided API documentation, tutorial and access to
 * the source code of the XebiaLabs plugins. You agree not to (i) lease, rent
 * or sublicense the Software or Documentation to any third party, or otherwise
 * use it except as permitted in this agreement; (ii) reverse engineer,
 * decompile, disassemble, or otherwise attempt to determine source code or
 * protocols from the Software, and/or to (iii) copy the Software or
 * Documentation (which includes the source code of the XebiaLabs plugins). You
 * shall not create or attempt to create any derivative works from the Software
 * except and only to the extent permitted by law. You will preserve XebiaLabs'
 * copyright and legal notices on the Software and Documentation. XebiaLabs
 * retains all rights not expressly granted to You in the Personal License
 * Agreement.
 */

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
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.Map;

/**
 * Builds a session to an unreachable host through a tunnel on the jumping station.
 */
public class TunnelledHostSession implements HostSession {
    private static final int DEFAULT_LOCAL_PORT = 9000;
    private static Logger logger = LoggerFactory.getLogger(TunnelledHostSession.class);

    private final int tunnelPort;
    private Session tunnel;
    private final HostSession tunnelledHostSession;

    public TunnelledHostSession(UnreachableHost host) {
        tunnelPort = startTunnel(host);
        tunnelledHostSession = BrokenHostSessionFactory.getHostSession(host.getOperatingSystemFamily(), host.getAccessMethod(), 
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
    	if (tunnelledHostSession != null) {
    		tunnelledHostSession.close();
    	}
    	if (tunnel != null) {
    		tunnel.disconnect();
    		logger.info(String.format("Tunnel on port %d via %s closed", tunnelPort, tunnel.getHost()));
    	}
    }

    public OperatingSystemFamily getHostOperatingSystem() {
        return tunnelledHostSession.getHostOperatingSystem();
    }

    public HostFile getFile(String hostPath) throws RuntimeIOException {
        return tunnelledHostSession.getFile(hostPath);
    }

    public HostFile getFile(HostFile parent, String child) throws RuntimeIOException {
        return tunnelledHostSession.getFile(parent, child);
    }

    public HostFile getTempFile(String nameTemplate) throws RuntimeIOException {
        return tunnelledHostSession.getTempFile(nameTemplate);
    }

    public HostFile getTempFile(String prefix, String suffix) throws RuntimeIOException {
        return tunnelledHostSession.getTempFile(prefix, suffix);
    }

    public int execute(CommandExecutionCallbackHandler handler, String... commandLine) throws RuntimeIOException {
        return tunnelledHostSession.execute(handler, commandLine);
    }

    public int execute(CommandExecutionCallbackHandler handler, Map<String, String> inputResponse, String... commandLine) {
        return tunnelledHostSession.execute(handler, inputResponse, commandLine);
    }

    public CommandExecution startExecute(String... commandLine) {
        return tunnelledHostSession.startExecute(commandLine);
    }

    public HostFile copyToTemporaryFile(File localFile) throws RuntimeIOException {
        return tunnelledHostSession.copyToTemporaryFile(localFile);
    }

    public HostFile copyToTemporaryFile(Resource resource) throws RuntimeIOException {
        return tunnelledHostSession.copyToTemporaryFile(resource);
    }
}
