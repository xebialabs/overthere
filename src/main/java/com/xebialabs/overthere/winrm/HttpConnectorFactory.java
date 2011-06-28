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
package com.xebialabs.overthere.winrm;

import com.xebialabs.overthere.winrm.connector.JdkHttpConnector;
import com.xebialabs.overthere.winrm.connector.JdkHttpsConnector;
import com.xebialabs.overthere.winrm.connector.JdkLazyHttpsConnector;

import java.net.URL;

/**
 */
public class HttpConnectorFactory {

	public static HttpConnector newHttpConnector(WinRMHost host) {
		switch (host.getProtocol()) {
			case HTTP:
				return new JdkHttpConnector(host);
			case HTTPS:
				return new JdkHttpsConnector(host);
			case HTTPS_LAZY:
				return new JdkLazyHttpsConnector(host);
		}
		throw new IllegalArgumentException("Unsupported protocol " + host.getProtocol());
	}

	public static HttpConnector newHttpConnector(Protocol p, URL targetURL, TokenGenerator tokenGenerator) {
		switch (p) {
			case HTTP:
				return new JdkHttpConnector(targetURL, tokenGenerator);
			case HTTPS:
				return new JdkHttpsConnector(targetURL, tokenGenerator);
			case HTTPS_LAZY:
				return new JdkLazyHttpsConnector(targetURL, tokenGenerator);
		}
		throw new IllegalArgumentException("Unsupported protocol " + p);
	}
}
