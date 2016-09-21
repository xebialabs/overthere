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
package com.xebialabs.overthere.winrm;

import java.util.HashMap;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

class KerberosJaasConfiguration extends Configuration {

    private boolean debug;
    private boolean ticketCache;

    KerberosJaasConfiguration(boolean debug) {
        this.debug = debug;
        this.ticketCache = false;
    }

    KerberosJaasConfiguration(boolean debug, boolean ticketCache) {
        this.debug = debug;
        this.ticketCache = ticketCache;
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String s) {
        final HashMap<String, String> options = new HashMap<String, String>();

        if (debug) {
            options.put("debug", "true");
        }

        options.put("refreshKrb5Config", "true");

        if (JavaVendor.isIBM()) {
            options.put("credsType", "initiator");

            options.put("useDefaultCcache", String.valueOf(ticketCache));
        } else {
            options.put("client", "true");
            options.put("useKeyTab", "false");
            options.put("doNotPrompt", "false");

            options.put("useTicketCache", String.valueOf(ticketCache));
        }

        return new AppConfigurationEntry[]{new AppConfigurationEntry(JavaVendor.getKrb5LoginModuleName(),
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options)};
    }



}
