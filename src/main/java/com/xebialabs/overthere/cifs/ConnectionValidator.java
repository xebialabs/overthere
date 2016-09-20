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
package com.xebialabs.overthere.cifs;

import com.xebialabs.overthere.OperatingSystemFamily;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.util.OverthereUtils.checkArgument;

public abstract class ConnectionValidator {

    public static void checkIsWindowsHost(OperatingSystemFamily os, String protocol, CifsConnectionType connectionType) {
        checkArgument(os == WINDOWS, "Cannot create a " + protocol + ":%s connection to a host that is not running Windows", connectionType.toString().toLowerCase());
    }

    public static void checkNotNewStyleWindowsDomain(String username, String protocol, CifsConnectionType connectionType) {
        checkArgument(!username.contains("@"), "Cannot create a " + protocol + ":%s connection with a new-style Windows domain account [%s], use DOMAIN\\USER instead.", connectionType.toString().toLowerCase(), username);
    }

    public static void checkNotOldStyleWindowsDomain(String username, String protocol, CifsConnectionType connectionType) {
        checkArgument(!username.contains("\\"), "Cannot create a " + protocol + ":%s connection with an old-style Windows domain account [%s], use USER@DOMAIN instead.", connectionType.toString().toLowerCase(), username);
    }

    public static void checkNotThroughJumpstation(AddressPortMapper mapper, String protocol, CifsConnectionType connectionType) {
        checkArgument(mapper instanceof DefaultAddressPortMapper, "Cannot create a " + protocol + ":%s connection when connecting through a SSH jumpstation", connectionType.toString().toLowerCase());

    }

    public static void checkNoSingleQuoteInPassword(String password, String protocol, CifsConnectionType connectionType) {
        checkArgument(password.indexOf('\'') == -1 && password.indexOf('\"') == -1, "Cannot create a " + protocol + ":%s connection with a password that contains a single quote (\') or a double quote (\")", connectionType.toString().toLowerCase());
    }
}

