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

import java.net.URL;

/**
 * Default HTTPS connector
 * Need to import server certificates in the client's trust store.
 * http://blog.ippon.fr/2008/10/20/certificats-auto-signe-et-communication-ssl-en-java/
 */
public class JdkHttpsConnector extends  JdkHttpConnector {

	public JdkHttpsConnector(URL targetURL, TokenGenerator tokenGenerator) {
		super(targetURL, tokenGenerator);
	}
}
