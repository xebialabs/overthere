package com.xebialabs.overthere.gcp.credentials;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.ServiceAccountCredentials;

class ServiceAccountPkcs8GcpCredentialFactory extends GcpCredentialFactory {

    private final String clientId;
    private final String clientEmail;
    private final String privateKeyPkcs8;
    private final String privateKeyId;
    private final Collection<String> scopes;
    private final HttpTransportFactory transportFactory;
    private final URI tokenServerUri;
    private final String serviceAccountUser;

    ServiceAccountPkcs8GcpCredentialFactory(final String clientId,
                                            final String clientEmail,
                                            final String privateKeyPkcs8,
                                            final String privateKeyId,
                                            final Collection<String> scopes,
                                            final HttpTransportFactory transportFactory,
                                            final URI tokenServerUri,
                                            final String serviceAccountUser) {
        this.clientId = clientId;
        this.clientEmail = clientEmail;
        this.privateKeyPkcs8 = privateKeyPkcs8;
        this.privateKeyId = privateKeyId;
        this.scopes = scopes;
        this.transportFactory = transportFactory;
        this.tokenServerUri = tokenServerUri;
        this.serviceAccountUser = serviceAccountUser;
    }

    @Override
    public ProjectCredentials doCreate() {
        try {
            ServiceAccountCredentials serviceAccountCredentials = ServiceAccountCredentials.fromPkcs8(
                    clientId, clientEmail, privateKeyPkcs8, privateKeyId, scopes, transportFactory, tokenServerUri, serviceAccountUser);
            return new ProjectCredentials(
                    serviceAccountCredentials,
                    serviceAccountCredentials.getProjectId(),
                    serviceAccountCredentials.getClientEmail());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load private key for clientEmail " + clientEmail, e);
        }
    }

    @Override
    public String info() {
        return "PKCS8 key for clientEmail " + clientEmail;
    }
}
