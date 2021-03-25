package com.xebialabs.overthere.gcp.credentials;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import com.google.common.collect.Sets;

import com.xebialabs.overthere.ConnectionOptions;

import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CLIENT_EMAIL;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CLIENT_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_JSON;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.PRIVATE_KEY_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.PRIVATE_KEY_PKCS8;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.PROJECT_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.SCOPES;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.SERVICE_ACCOUNT_USER;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.TOKEN_EXPIRATION_TIME_MILLIS;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.TOKEN_SERVER_URI;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.TOKEN_VALUE;

public enum GcpCredentialsType {

    Default(Sets.newHashSet(PROJECT_ID, CLIENT_EMAIL)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            return new DefaultCredentialsGcpCredentialFactory(
                    options.<String>get(PROJECT_ID),
                    options.<String>get(CLIENT_EMAIL)
            );
        }
    },
    ServiceAccountJsonFile(Sets.newHashSet(CREDENTIALS_FILE)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            return new ServiceAccountFileGcpCredentialFactory(
                    new File(options.<String>get(CREDENTIALS_FILE))
            );
        }
    },
    ServiceAccountJson(Sets.newHashSet(CREDENTIALS_JSON)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            return new ServiceAccountJsonGcpCredentialFactory(
                    options.<String>get(CREDENTIALS_JSON)
            );
        }
    },
    ServiceAccountPkcs8(Sets.newHashSet(CLIENT_ID, CLIENT_EMAIL, PRIVATE_KEY_PKCS8, PRIVATE_KEY_ID, SCOPES)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            final String clientId = options.get(CLIENT_ID);
            final String clientEmail = options.get(CLIENT_EMAIL);
            final String privateKeyPkcs8 = options.get(PRIVATE_KEY_PKCS8);
            final String privateKeyId = options.get(PRIVATE_KEY_ID);
            final Collection<String> scopes = Arrays.asList(options.<String>get(SCOPES).split(","));
            final URI tokenServerUri = URI.create(options.<String>getOptional(TOKEN_SERVER_URI));
            final String serviceAccountUser = options.getOptional(SERVICE_ACCOUNT_USER);
            return new ServiceAccountPkcs8GcpCredentialFactory(
                    clientId, clientEmail, privateKeyPkcs8, privateKeyId, scopes, null, tokenServerUri, serviceAccountUser
            );
        }
    },
    AccessToken(Sets.newHashSet(TOKEN_VALUE, TOKEN_EXPIRATION_TIME_MILLIS, PROJECT_ID, CLIENT_EMAIL)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            String tokenValue = options.get(TOKEN_VALUE);
            long tokenExpirationTimeMs = options.get(TOKEN_EXPIRATION_TIME_MILLIS);
            return new AccessTokenGcpCredentialFactory(
                    new com.google.auth.oauth2.AccessToken(tokenValue, new Date(tokenExpirationTimeMs)),
                    options.<String>get(PROJECT_ID),
                    options.<String>get(CLIENT_EMAIL)
            );
        }
    },
    IdToken(Sets.newHashSet(TOKEN_VALUE, PROJECT_ID, CLIENT_EMAIL)) {
        @Override
        protected GcpCredentialFactory doCreate(final ConnectionOptions options) {
            String tokenValue = options.get(TOKEN_VALUE);
            try {
                return new AccessTokenGcpCredentialFactory(
                        com.google.auth.oauth2.IdToken.create(tokenValue),
                        options.<String>get(PROJECT_ID),
                        options.<String>get(CLIENT_EMAIL)
                );
            } catch (IOException e) {
                throw new IllegalArgumentException("Token value is not valid " + tokenValue, e);
            }
        }
    };

    private final Set<String> requiredOptions;

    GcpCredentialsType(final Set<String> requiredOptions) {
        this.requiredOptions = requiredOptions;
    }

    public void validateOptions(final ConnectionOptions options) {
        for (String requiredOption : requiredOptions) {
            if (!options.containsKey(requiredOption)) {
                throw new IllegalArgumentException("For gcp credentials type " + name() + " is missing required option " + requiredOption);
            }
        }
    }

    protected abstract GcpCredentialFactory doCreate(final ConnectionOptions options);

    public GcpCredentialFactory create(final ConnectionOptions options) {
        validateOptions(options);
        return doCreate(options);
    }
}
