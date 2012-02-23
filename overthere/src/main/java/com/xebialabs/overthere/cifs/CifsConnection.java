/*
 * This file is part of Overthere.
 * 
 * Overthere is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Overthere is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Overthere.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PATH_SHARE_MAPPINGS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PATH_SHARE_MAPPINGS_DEFAULT;
import static java.net.InetSocketAddress.createUnresolved;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.xebialabs.overthere.spi.AddressPortResolver;
import jcifs.smb.NtlmPasswordAuthentication;
import com.xebialabs.overthere.*;
import com.xebialabs.overthere.spi.BaseOverthereConnection;
import jcifs.smb.SmbFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for connections to a Windows host using CIFS.
 * 
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator access is needed.</li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public abstract class CifsConnection extends BaseOverthereConnection {

	protected CifsConnectionType cifsConnectionType;

	protected String address;
	
	protected int cifsPort;
	
	protected int port;

	protected String username;

	protected String password;
	
	protected PathEncoder encoder;

	protected NtlmPasswordAuthentication authentication;

	/**
	 * Creates a {@link CifsConnection}. Don't invoke directly. Use {@link Overthere#getConnection(String, ConnectionOptions)} instead.
	 */
	public CifsConnection(String protocol, ConnectionOptions options, AddressPortResolver resolver, boolean canStartProcess) {
		super(protocol, options, resolver, canStartProcess);
		this.cifsConnectionType = options.get(CONNECTION_TYPE);
		String address = options.get(ADDRESS);
		InetSocketAddress addressPort = resolver.resolve(createUnresolved(address, options.get(PORT, getDefaultPort())));
		this.address = addressPort.getHostName();
		this.port = addressPort.getPort();
		this.username = options.get(USERNAME);
		this.password = options.get(PASSWORD);
		InetSocketAddress addressCifsPort = resolver.resolve(createUnresolved(address, options.get(CIFS_PORT, DEFAULT_CIFS_PORT)));
		this.cifsPort = addressCifsPort.getPort();
		this.encoder = new PathEncoder(null, null, this.address, cifsPort, options.get(PATH_SHARE_MAPPINGS, PATH_SHARE_MAPPINGS_DEFAULT));
		this.authentication = new NtlmPasswordAuthentication(null, username, password);
	}

	private Integer getDefaultPort() {
		switch(cifsConnectionType) {
		case TELNET:
			return CifsConnectionBuilder.DEFAULT_TELNET_PORT;
		case WINRM_HTTP:
			return CifsConnectionBuilder.DEFAULT_WINRM_HTTP_PORT;
		case WINRM_HTTPS:
			return CifsConnectionBuilder.DEFAULT_WINRM_HTTPS_PORT;
		default:
			throw new IllegalArgumentException("Unknown CIFS connection type " + cifsConnectionType);	
		}
    }

	@Override
	public void doClose() {
		// no-op
	}

	@Override
	public OverthereFile getFile(String hostPath) throws RuntimeIOException {
		try {
			SmbFile smbFile = new SmbFile(encodeAsSmbUrl(hostPath), authentication);
			return new CifsFile(this, smbFile);
		} catch (IOException exc) {
			throw new RuntimeIOException(exc);
		}
	}

	@Override
	public OverthereFile getFile(OverthereFile parent, String child) throws RuntimeIOException {
		StringBuilder childPath = new StringBuilder();
		childPath.append(parent.getPath());
		if(!parent.getPath().endsWith(getHostOperatingSystem().getFileSeparator())) {
			childPath.append(getHostOperatingSystem().getFileSeparator());
		}
		childPath.append(child.replace('\\', '/'));
		return getFile(childPath.toString());
	}

    @Override
    protected OverthereFile getFileForTempFile(OverthereFile parent, String name) {
    	return getFile(parent, name);
    }

	private String encodeAsSmbUrl(String hostPath) {
		try {
			String smbUrl = encoder.toSmbUrl(hostPath);
			logger.trace("Encoded Windows host path {} to SMB URL {}", hostPath, maskSmbUrl(smbUrl));
			return smbUrl;
		} catch (IllegalArgumentException exception) {
			throw new RuntimeIOException(exception);
		}
	}

	private String maskSmbUrl(String smbUrl) {
	    return smbUrl.replace(password, "********");
    }

    @Override
    public String toString() {
        return "cifs:" + cifsConnectionType.toString().toLowerCase() + "://" + username + "@" + address + ":" + cifsPort + ":" + port;
    }

	private static Logger logger = LoggerFactory.getLogger(CifsConnection.class);

}
