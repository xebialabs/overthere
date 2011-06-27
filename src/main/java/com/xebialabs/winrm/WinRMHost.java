/*
 * This file is part of WinRM.
 *
 * WinRM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WinRM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WinRM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.xebialabs.winrm;

import com.xebialabs.winrm.exception.WinRMRuntimeIOException;
import com.xebialabs.winrm.tokengenerator.KerberosTokenGenerator;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The WinRMHost class gathers all the properties of the target remote WinRM server.
 */
public class WinRMHost implements Serializable {

	public static final int DEFAULT_HTTP_PORT = 5985;

	public static final int DEFAULT_HTTPS_PORT = 5986;

	static final String DEFAULT_WINRM_CONTEXT = "/wsman";

	private String host;

	private int port;

	private String username;

	private String password;

	private Protocol protocol;

	private AuthenticationMode authenticationMode = AuthenticationMode.KERBEROS;

	private String context = DEFAULT_WINRM_CONTEXT;


	public WinRMHost(String host, int port, String username, String password) {
		this(Protocol.HTTP, host, port, username, password);
	}

	public WinRMHost(Protocol protocol, String host, int port, String username, String password) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public AuthenticationMode getAuthenticationMode() {
		return authenticationMode;
	}

	public void setAuthenticationMode(AuthenticationMode authenticationMode) {
		this.authenticationMode = authenticationMode;
	}

	public URL getTargetURL() {
		try {
			return new URL(protocol.get(), host, port, context);
		} catch (MalformedURLException e) {
			throw new WinRMRuntimeIOException("Cannot build a new URL using host " + this, e);
		}
	}

	@Override
	public String toString() {
		return "WinRMHost{" +
				"host='" + host + '\'' +
				", port=" + port +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", protocol=" + protocol +
				", authenticationMode=" + authenticationMode +
				", context='" + context + '\'' +
				'}';
	}

	public TokenGenerator getTokenGenerator() {
		switch (this.authenticationMode) {
			case KERBEROS:
				return new KerberosTokenGenerator(host, username, password);
		}
		throw new IllegalArgumentException("the " + this.authenticationMode + " is not supported");

	}
}
