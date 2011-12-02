/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2011 XebiaLabs
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xebialabs.overthere.cifs;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;

import java.io.IOException;

import jcifs.smb.SmbFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.RuntimeIOException;

/**
 * Base class for connections to a Windows host using CIFS.
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
	
	protected PathEncoder encoder;

	/**
	 * Creates a {@link CifsConnection}. Don't invoke directly. Use {@link Overthere#getConnection(String, ConnectionOptions)} instead.
	 */
	public CifsConnection(String protocol, ConnectionOptions options, boolean canStartProcess) {
		super(protocol, options, canStartProcess);
		this.cifsConnectionType = options.get(CONNECTION_TYPE);
		this.address = options.get(ADDRESS);
		this.port = options.get(PORT, getDefaultPort());
		this.username = options.get(USERNAME);
		this.password = options.get(PASSWORD);
		this.cifsPort = options.get(CIFS_PORT, DEFAULT_CIFS_PORT);
		this.encoder = new PathEncoder(username, password, address, port);
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
	    try {
	        String smbUrl = encoder.toSmbUrl(hostPath);
	        logger.trace("Encoded Windows host path {} to SMB URL {}", hostPath, smbUrl.toString());
	        return smbUrl;
	    } catch (IllegalArgumentException exception) {
                throw new RuntimeIOException(exception);
            }
	}

    @Override
    public String toString() {
        return "cifs:" + cifsConnectionType.toString().toLowerCase() + "://" + username + "@" + address + ":" + cifsPort + ":" + port;
    }

	private static Logger logger = LoggerFactory.getLogger(CifsConnection.class);

}

