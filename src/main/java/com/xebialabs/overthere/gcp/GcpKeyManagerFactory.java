package com.xebialabs.overthere.gcp;

import java.util.Map;
import java.util.WeakHashMap;

public class GcpKeyManagerFactory {

    private static final Map<String, GcpKeyManager> managers = new WeakHashMap<>();

    private static final GenerateSshKey GENERATE_SSH_KEY = new JCraftGenerateSshKey();

    public static GcpKeyManager create(final String credentialsFile) {
        synchronized (managers) {
            GcpKeyManager gcpKeyManager = managers.get(credentialsFile);
            if (gcpKeyManager == null) {
                gcpKeyManager = new GcpOsLoginKeyManager(GENERATE_SSH_KEY, credentialsFile).init();
                managers.put(credentialsFile, gcpKeyManager);
            }
            return gcpKeyManager;
        }
    }
}
