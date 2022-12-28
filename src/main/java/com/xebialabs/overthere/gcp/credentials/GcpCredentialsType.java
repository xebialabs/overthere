package com.xebialabs.overthere.gcp.credentials;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import com.google.common.collect.Sets;

import com.xebialabs.overthere.ConnectionOptions;

import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CLIENT_EMAIL;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CLIENT_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_JSON;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_CREDENTIALS_TYPE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.PRIVATE_KEY_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.PRIVATE_KEY_PKCS8;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.PROJECT_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.SCOPES;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.SERVICE_ACCOUNT_USER;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.TOKEN_SERVER_URI;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.API_TOKEN;

/**
 * Enums that are used under option `gcpCredentialsType`.
 */
public enum GcpCredentialsType {

    Default(Sets.newHashSet(PROJECT_ID, CLIENT_EMAIL)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            return new DefaultCredentialsGcpCredentialFactory(
                    options.<String>get(PROJECT_ID),
                    options.<String>get(CLIENT_EMAIL)
            );
        }

        @Override
        public String createKey(final ConnectionOptions options) {
            return composeKey(options.<String>get(CLIENT_EMAIL), options);
        }
    },
    ServiceAccountToken(Sets.newHashSet(PROJECT_ID, API_TOKEN, CLIENT_EMAIL)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            return new ServiceAccountTokenGcpCredentialFactory(
                    options.<String>get(PROJECT_ID),
                    options.<String>get(API_TOKEN),
                    options.<String>get(CLIENT_EMAIL)
            );
        }
        @Override
        public String createKey(final ConnectionOptions options) {
            return composeKey(options.<String>get(API_TOKEN), options);
        }
    },
    ServiceAccountJsonFile(Sets.newHashSet(CREDENTIALS_FILE)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            return new ServiceAccountFileGcpCredentialFactory(
                    new File(options.<String>get(CREDENTIALS_FILE))
            );
        }

        @Override
        public String createKey(final ConnectionOptions options) {
            return composeKey(options.<String>get(CREDENTIALS_FILE), options);
        }
    },
    ServiceAccountJson(Sets.newHashSet(CREDENTIALS_JSON)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            return new ServiceAccountJsonGcpCredentialFactory(
                    options.<String>get(CREDENTIALS_JSON)
            );
        }

        @Override
        public String createKey(final ConnectionOptions options) {
            return composeKey(options.<String>get(CREDENTIALS_JSON), options);
        }
    },
    ServiceAccountPkcs8(Sets.newHashSet(PROJECT_ID, CLIENT_ID, CLIENT_EMAIL, PRIVATE_KEY_PKCS8, PRIVATE_KEY_ID)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            final String projectId = options.get(PROJECT_ID);
            final String clientId = options.get(CLIENT_ID);
            final String clientEmail = options.get(CLIENT_EMAIL);
            final String privateKeyPkcs8 = options.get(PRIVATE_KEY_PKCS8);
            final String privateKeyId = options.get(PRIVATE_KEY_ID);
            String scopesString = options.getOptional(SCOPES);
            Collection<String> scopes = Collections.emptyList();
            if (scopesString != null) {
                scopes = Arrays.asList(scopesString.split(","));
            }
            final URI tokenServerUri = URI.create(options.<String>getOptional(TOKEN_SERVER_URI));
            final String serviceAccountUser = options.getOptional(SERVICE_ACCOUNT_USER);
            return new ServiceAccountPkcs8GcpCredentialFactory(
                    projectId, clientId, clientEmail, privateKeyPkcs8, privateKeyId, scopes, null, tokenServerUri, serviceAccountUser
            );
        }

        @Override
        public String createKey(final ConnectionOptions options) {
            return composeKey(options.<String>get(PRIVATE_KEY_ID), options);
        }
    };

    private final Set<String> requiredOptions;

    GcpCredentialsType(final Set<String> requiredOptions) {
        this.requiredOptions = requiredOptions;
    }

    /**
     * Validates if options have all required keys.
     *
     * @param options that will be validated
     * @throws IllegalArgumentException in case of missing option.
     */
    public void validateOptions(final ConnectionOptions options) {
        for (String requiredOption : requiredOptions) {
            if (!options.containsKey(requiredOption)) {
                throw new IllegalArgumentException("For gcp credentials type " + name() + " is missing required option " + requiredOption);
            }
        }
    }

    protected abstract GcpCredentialFactory doCreate(final ConnectionOptions options);

    protected String composeKey(final String keySuffix, final ConnectionOptions options) {
        return name() + ":" + keySuffix + ":" + options.get(ConnectionOptions.USERNAME, "");
    }

    /**
     * Creates GcpCredentialFactory from this type and valid options
     *
     * @param options valid options that are required for creation
     * @return GcpCredentialFactory according to this type and valid options.
     */
    public GcpCredentialFactory createGcpCredentialFactory(final ConnectionOptions options) {
        validateOptions(options);
        return doCreate(options);
    }

    /**
     * Create key that will be used as key in the GcpKeyManagerFactory
     *
     * @param options valid options that are required for key creation
     * @return unique string according to provided options
     */
    public abstract String createKey(final ConnectionOptions options);

    /**
     * Resolves credentials type from option `gcpCredentialsType`.
     *
     * @param options valid options that are required for resolved type
     * @return resolved type
     */
    public static GcpCredentialsType resolve(final ConnectionOptions options) {
        return options.getEnum(GCP_CREDENTIALS_TYPE, GcpCredentialsType.class);
    }
}
