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
package com.xebialabs.overthere.cifs.winrm.connector;

import com.xebialabs.overthere.cifs.winrm.TokenGenerator;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

/**
 * Lax HTTPS Connection
 */
public class LaxJdkHttpConnector extends JdkHttpConnector {

	static {
		// FIXME: Every HTTP connection opened after this static initializer is called will be "lazy".
		HttpsURLConnection.setDefaultHostnameVerifier(new LaxHostnameVerifier());
		HttpsURLConnection.setDefaultSSLSocketFactory(new LaxSSLSocketFactory());
	}

	public LaxJdkHttpConnector(URL targetURL, TokenGenerator tokenGenerator) {
		super(targetURL, tokenGenerator);
	}

}
