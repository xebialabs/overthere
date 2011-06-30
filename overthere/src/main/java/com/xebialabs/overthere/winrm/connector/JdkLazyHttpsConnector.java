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
package com.xebialabs.overthere.winrm.connector;

import com.xebialabs.overthere.winrm.TokenGenerator;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

/**
 * Lazy SSL Connection ....
 */
public class JdkLazyHttpsConnector extends JdkHttpConnector {

	static {
		HttpsURLConnection.setDefaultHostnameVerifier(new LazyHostnameVerifier());
		HttpsURLConnection.setDefaultSSLSocketFactory(new LazySSLSocketFactory());
	}

	public JdkLazyHttpsConnector(URL targetURL, TokenGenerator tokenGenerator) {
		super(targetURL, tokenGenerator);
	}
}
