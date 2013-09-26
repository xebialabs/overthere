package com.xebialabs.overthere.cifs.winrm;

import java.util.HashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

class KerberosJaasConfiguration extends Configuration {

    private boolean debug;

    KerberosJaasConfiguration(boolean debug) {
        this.debug = debug;
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String s) {
        final HashMap<String, String> options = new HashMap<String, String>();
        options.put("client", "true");
        options.put("useTicketCache", "false");
        options.put("useKeyTab", "false");
        options.put("doNotPrompt", "false");
        options.put("refreshKrb5Config", "true");
        if (debug) {
            options.put("debug", "true");
        }
        return new AppConfigurationEntry[] { new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, options) };
    }

}
