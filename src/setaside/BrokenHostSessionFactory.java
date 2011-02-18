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

import com.xebialabs.overthere.cifs.CifsTelnetHostConnection;
import com.xebialabs.overthere.local.LocalHostConnection;
import com.xebialabs.overthere.ssh.*;
import org.apache.commons.lang.StringUtils;

import com.xebialabs.overthere.ssh.SshInteractiveSudoHostConnection;
import com.xebialabs.overthere.ssh.SshSftpHostConnection;

/**
 * Factory for {@linkplain HostConnection host sessions}.
 */
public class BrokenHostSessionFactory {

	/**
	 * The default port to use for SSH host sessions
	 */
	public static final int DEFAULT_SSH_PORT = 22;

	/**
	 * The default timeout for opening a connection in milliseconds.
	 */
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 120000;

	/**
	 * Creates a host connection for the host.
	 * 
	 * @param osFamily
	 *            the OS family of the host
	 * @param accessMethod
	 *            the way the host is accessed, e.g. local access, SSH w/ SFTP, SSH w/ SCP, SSH w/ SUDO, etc.
	 * @param address
	 *            the address of the host
	 * @param port
	 *            the port on the host to connect with
	 * @param username
	 *            the username to connect to the host
	 * @param password
	 *            the password to connect to the host
	 * @param sudoUsername
	 *            the username to sudo to
	 * @param temporaryDirectoryPath
	 *            the path of the directory in which to store temporary files
	 * @return the connection created
	 * @throws IllegalStateException
	 *             if no suitable connection can be created.
	 */
	public static HostConnection getHostSession(OperatingSystemFamily osFamily, HostAccessMethod accessMethod, String address, int port, String username,
	        String password, String sudoUsername, String temporaryDirectoryPath) {
		HostConnection s;
		switch (accessMethod) {
		case NONE:
			throw new IllegalStateException("Cannot connect to a host that has a NONE access method");
		case LOCAL:
			s = new LocalHostConnection(osFamily, temporaryDirectoryPath);
			break;
		case SSH_SFTP:
			s = new SshSftpHostConnection(osFamily, temporaryDirectoryPath, address, port, username, password);
			break;
		case SSH_SCP:
			s = new SshScpHostConnection(osFamily, temporaryDirectoryPath, address, port, username, password);
			break;
		case SSH_SUDO:
			s = new SshSudoHostConnection(osFamily, temporaryDirectoryPath, address, port, username, password, sudoUsername);
			break;
		case SSH_INTERACTIVE_SUDO:
			s = new SshInteractiveSudoHostConnection(osFamily, temporaryDirectoryPath, address, port, username, password, sudoUsername);
			break;
		case CIFS_TELNET:
			s = new CifsTelnetHostConnection(osFamily, temporaryDirectoryPath, address, port, username, password);
			break;
		default:
			throw new IllegalStateException("Unknown host access method " + accessMethod);
		}
		return s;
	}

	/**
	 * Creates a host connection for the host.
	 * 
	 * @param osFamily
	 *            the OS family of the host
	 * @param accessMethod
	 *            the way the host is accessed, e.g. local access, SSH w/ SFTP, SSH w/ SCP, SSH w/ SUDO, etc.
	 * @param hostSpecification
	 *            the host to connect with, specified as a host address optionally followed by a colon and a port number
	 * @param username
	 *            the username to connect to the host
	 * @param password
	 *            the password to connect to the host
	 * @param sudoUsername
	 *            the username to sudo to
	 * @param temporaryDirectoryPath
	 *            the path of the directory in which to store temporary files
	 * @return the connection created
	 * @throws IllegalArgumentException
	 *             if the host specification contains an error
	 * @throws IllegalStateException
	 *             if no suitable connection can be created.
	 */
	public static HostConnection getHostSession(OperatingSystemFamily osFamily, HostAccessMethod accessMethod, String hostSpecification, String username,
	        String password, String sudoUsername, String temporaryDirectoryPath) {
		String address;
		int port;
		if (StringUtils.isBlank(hostSpecification)) {
			address = "localhost";
			port = -1;
		} else {
			// get address and port
			int pos = hostSpecification.indexOf(':');
			if (pos > 0 && pos < hostSpecification.length()) {
				address = hostSpecification.substring(0, pos);
				try {
					port = Integer.parseInt(hostSpecification.substring(pos + 1));
				} catch (NumberFormatException ignore) {
					throw new IllegalArgumentException("Host specification " + hostSpecification + " has an invalid port number");
				}
			} else {
				address = hostSpecification;
				port = DEFAULT_SSH_PORT;
			}
		}

		if (temporaryDirectoryPath == null || temporaryDirectoryPath.length() == 0) {
			temporaryDirectoryPath = getDefaultTemporaryDirectoryPath(osFamily, accessMethod);
		}

		return getHostSession(osFamily, accessMethod, address, port, username, password, sudoUsername, temporaryDirectoryPath);
	}

	/**
	 * Returns an HostConnection based on the information in a Host CI.
	 * 
	 * @param host
	 *            the host from which to take the details.
	 * @return the connection created
	 * @throws IllegalArgumentException
	 *             if the host specification contains an error
	 * @throws IllegalStateException
	 *             if no suitable connection can be created.
	 */
	public static HostConnection getHostSession(Host host) {
		if (host instanceof UnreachableHost) {
			return new TunnelledHostConnection((UnreachableHost) host);
		} else {
			return getHostSession(host.getOperatingSystemFamily(), host.getAccessMethod(), host.getAddress(), host.getUsername(), host.getPassword(),
			        host.getSudoUsername(), host.getTemporaryDirectoryLocation());
		}
	}

	private static String getDefaultTemporaryDirectoryPath(OperatingSystemFamily osFamily, HostAccessMethod accessMethod) {
		if (accessMethod == HostAccessMethod.LOCAL) {
			return System.getProperty("java.io.tmpdir", osFamily.getDefaultTemporaryDirectoryPath());
		} else {
			return osFamily.getDefaultTemporaryDirectoryPath();
		}
	}

}
