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

import java.util.ArrayList;
import java.util.List;

/**
 */
public enum OptionSet {

    OPEN_SHELL(new KeyValuePair("WINRS_NOPROFILE", "FALSE"), new KeyValuePair("WINRS_CODEPAGE", "437")),
    RUN_COMMAND(new KeyValuePair("WINRS_CONSOLEMODE_STDIN", "TRUE"));

    private final List<KeyValuePair> keyValuePairs;

    OptionSet(KeyValuePair... keyValuePairs) {
        this.keyValuePairs = new ArrayList<KeyValuePair>();
        for (KeyValuePair keyValuePair : keyValuePairs) {
            this.keyValuePairs.add(keyValuePair);
        }
    }

    public List<KeyValuePair> getKeyValuePairs() {
        return keyValuePairs;
    }
}
