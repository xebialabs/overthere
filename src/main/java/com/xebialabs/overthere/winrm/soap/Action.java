/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.winrm.soap;

import java.net.URI;

public enum Action {

    WS_ACTION("http://schemas.xmlsoap.org/ws/2004/09/transfer/Create"),
    WS_COMMAND("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Command"),
    WS_RECEIVE("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Receive"),
    WS_SEND("http://schemas.microsoft.com/wbem/wsman/1/windows/shell/Send"),
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
