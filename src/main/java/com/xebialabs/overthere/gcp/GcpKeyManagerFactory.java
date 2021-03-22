package com.xebialabs.overthere.gcp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GcpKeyManagerFactory {

    private static final ConcurrentMap<String, GcpKeyManager> managers = new ConcurrentHashMap<>();

    private static final GenerateSshKey GENERATE_SSH_KEY = new JCraftGenerateSshKey();

    public static GcpKeyManager create(final String credentialsFile) {
        GcpKeyManager gcpKeyManager = managers.get(credentialsFile);
        if (gcpKeyManager == null) {
            gcpKeyManager = new GcpOsLoginKeyManager(GENERATE_SSH_KEY, credentialsFile).init();
            managers.putIfAbsent(credentialsFile, gcpKeyManager);
        }
        return gcpKeyManager;
    }
}
