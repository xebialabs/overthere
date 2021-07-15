package com.xebialabs.overthere.gcp;

import java.util.Map;
import java.util.WeakHashMap;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialFactory;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialsType;

public class GcpKeyManagerFactory {

    private static final Map<String, GcpKeyManager> managers = new WeakHashMap<>();

    static final GenerateSshKey GENERATE_SSH_KEY = new JCraftGenerateSshKey();

    public static GcpKeyManager create(final ConnectionOptions options) {

        GcpCredentialsType gcpCredentialsType = GcpCredentialsType.resolve(options);
        final String managersKey = gcpCredentialsType.createKey(options);

        synchronized (managers) {
            GcpKeyManager gcpKeyManager = managers.get(managersKey);
            if (gcpKeyManager == null) {
                GcpCredentialFactory gcpCredentialFactory = gcpCredentialsType.createGcpCredentialFactory(options);
                gcpKeyManager = GcpKeyManagementType.resolveGcpKeyManager(options, gcpCredentialFactory);
                managers.put(managersKey, gcpKeyManager);
            }
            return gcpKeyManager;
        }
    }
}
