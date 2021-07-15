package com.xebialabs.overthere.gcp;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialFactory;

import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.gcp.GcpKeyManagerFactory.GENERATE_SSH_KEY;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.APPLICATION_NAME;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_KEY_MANAGEMENT_TYPE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.INSTANCE_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.ZONE_NAME;

/**
 * Enum types that are under option `gcpKeyManagementType`
 */
public enum GcpKeyManagementType {
    OsLogin {
        @Override
        GcpKeyManager createGcpKeyManager(final ConnectionOptions options, final GcpCredentialFactory gcpCredentialFactory) {
            return new GcpOsLoginKeyManager(GENERATE_SSH_KEY, gcpCredentialFactory).init();
        }
    },
    Metadata {
        @Override
        GcpKeyManager createGcpKeyManager(final ConnectionOptions options, final GcpCredentialFactory gcpCredentialFactory) {
            String zoneName = options.get(ZONE_NAME);
            String instanceId = options.getOptional(INSTANCE_ID);
            String username = options.get(ConnectionOptions.USERNAME);
            String applicationName = options.getOptional(APPLICATION_NAME);
            return new GcpMetadataKeyManager(
                    GENERATE_SSH_KEY,
                    gcpCredentialFactory,
                    zoneName,
                    instanceId,
                    username,
                    applicationName
            ).init();
        }
    };

    abstract GcpKeyManager createGcpKeyManager(final ConnectionOptions options, final GcpCredentialFactory gcpCredentialFactory);

    /**
     * Resolves key management type from option `gcpKeyManagementType`.
     *
     * @param options reads `gcpKeyManagementType` option
     * @param gcpCredentialFactory credentials factory
     * @return created key manager according to supplied options.
     */
    public static GcpKeyManager resolveGcpKeyManager(final ConnectionOptions options, final GcpCredentialFactory gcpCredentialFactory) {
        GcpKeyManagementType gcpKeyManagementType = options.getEnum(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.class);
        return gcpKeyManagementType.createGcpKeyManager(options, gcpCredentialFactory);
    }
}
