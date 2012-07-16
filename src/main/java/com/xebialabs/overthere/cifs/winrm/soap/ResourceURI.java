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
package com.xebialabs.overthere.cifs.winrm.soap;

import java.net.URI;
import java.net.URISyntaxException;

public enum ResourceURI {

	RESOURCE_URI_CMD("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/cmd");

	private final String uri;

	ResourceURI(String uri) {
		this.uri = uri;
	}

	public URI getUri() throws URISyntaxException {
		return Soapy.getUri(uri);
	}
}
