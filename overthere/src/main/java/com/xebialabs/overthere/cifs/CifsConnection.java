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
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import jcifs.smb.SmbFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Base class for connections to a remote host using CIFS.
 * 
 * Limitations:
 * <ul>
 * <li>Shares with names like C$ need to available for all drives accessed. In practice, this means that Administrator access is needed.</li>
 * <li>Not tested with domain accounts.</li>
 * </ul>
 */
public abstract class CifsConnection extends OverthereConnection {

	protected CifsConnectionType cifsConnectionType;

	protected String address;
	
	protected int cifsPort;
	
	protected int port;

	protected String username;

	protected String password;

	/**
	 * Creates a {@link CifsConnection}. Don't invoke directly. Use {@link Overthere#getConnection(String, ConnectionOptions)} instead.
	 */
	public CifsConnection(String type, ConnectionOptions options) {
		super(type, options);
		this.cifsConnectionType = options.get(CONNECTION_TYPE);
		this.address = options.get(ADDRESS);
		this.port = options.get(PORT, getDefaultPort());
		this.username = options.get(USERNAME);
		this.password = options.get(PASSWORD);
		this.cifsPort = options.get(CIFS_PORT, DEFAULT_CIFS_PORT);
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
			SmbFile smbFile = new SmbFile(encodeAsSmbUrl(hostPath));
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
		StringBuffer smbUrl = new StringBuffer();
		smbUrl.append("smb://");
		smbUrl.append(urlEncode(username.replaceFirst("\\\\", ";")));
		smbUrl.append(":");
		smbUrl.append(urlEncode(password));
		smbUrl.append("@");
		smbUrl.append(urlEncode(address));
		if(cifsPort != DEFAULT_CIFS_PORT) {
			smbUrl.append(":");
			smbUrl.append(cifsPort);
		}
		smbUrl.append("/");

		if (hostPath.length() < 2) {
			throw new RuntimeIOException("Host path \"" + hostPath + "\" is too short");
		}

		if (hostPath.charAt(1) != ':') {
			throw new RuntimeIOException("Host path \"" + hostPath + "\" does not have a colon (:) as its second character");
		}
		smbUrl.append(hostPath.charAt(0));
		smbUrl.append("$/");
		if (hostPath.length() >= 3) {
			if (hostPath.charAt(2) != '\\') {
				throw new RuntimeIOException("Host path \"" + hostPath + "\" does not have a backslash (\\) as its third character");
			}
			smbUrl.append(hostPath.substring(3).replace('\\', '/'));
		}

		logger.trace("Encoded Windows host path {} to SMB URL {}", hostPath, smbUrl.toString());

		return smbUrl.toString();
	}

	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeIOException("Unable to construct SMB URL", e);
		}
	}

    @Override
    public String toString() {
        return "cifs:" + cifsConnectionType + "://" + username + "@" + address + ":" + port;
    }

	private static Logger logger = LoggerFactory.getLogger(CifsConnection.class);

}
