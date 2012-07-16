/* License added by: GRADLE-LICENSE-PLUGIN
 *
 * Copyright 2008-2012 XebiaLabs
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

package com.xebialabs.overthere.cifs.winrm.soap;

import java.net.URI;

public enum Action {

	WS_ACTION("http://schemas.xmlsoap.org/ws/2004/09/transfer/Create"),
	WS_COMMAND("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command"),
	WS_RECEIVE("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Receive"),
	WS_SIGNAL("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Signal"),
	WS_DELETE("http://schemas.xmlsoap.org/ws/2004/09/transfer/Delete");

	private String uri;

	Action(String uri) {
		this.uri = uri;
	}

	public URI getUri() {
		return Soapy.getUri(uri);
	}
}

