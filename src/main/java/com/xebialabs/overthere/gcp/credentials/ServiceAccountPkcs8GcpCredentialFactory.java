package com.xebialabs.overthere.gcp.credentials;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.ServiceAccountCredentials;

/**
 * Factory with minimum identifying information and custom transport using PKCS#8 for the private key.
 */
class ServiceAccountPkcs8GcpCredentialFactory extends GcpCredentialFactory {

    private final String projectId;
    private final String clientId;
    private final String clientEmail;
    private final String privateKeyPkcs8;
    private final String privateKeyId;
    private final Collection<String> scopes;
    private final HttpTransportFactory transportFactory;
    private final URI tokenServerUri;
    private final String serviceAccountUser;

    /**
     * @param projectId Project ID
     * @param clientId Client ID of the service account from the console
     * @param clientEmail Client email address of the service account from the console.
     * @param privateKeyPkcs8 RSA private key object for the service account in PKCS#8 format.
     * @param privateKeyId Private key identifier for the service account
     * @param scopes Scope strings for the APIs to be called. May be null or an empty collection, which results in a credential that must have createScoped called before use.
     * @param transportFactory HTTP transport factory, creates the transport used to get access tokens.
     * @param tokenServerUri URI of the end point that provides tokens.
     * @param serviceAccountUser The email of the user account to impersonate, if delegating domain-wide authority to the service account.
     */
    ServiceAccountPkcs8GcpCredentialFactory(final String projectId,
                                            final String clientId,
                                            final String clientEmail,
                                            final String privateKeyPkcs8,
                                            final String privateKeyId,
                                            final Collection<String> scopes,
                                            final HttpTransportFactory transportFactory,
                                            final URI tokenServerUri,
                                            final String serviceAccountUser) {
        this.projectId = projectId;
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
    protected ProjectCredentials doCreate() {
        try {
            ServiceAccountCredentials serviceAccountCredentials = ServiceAccountCredentials.fromPkcs8(
                    clientId, clientEmail, privateKeyPkcs8, privateKeyId, scopes, transportFactory, tokenServerUri, serviceAccountUser);
            return new ProjectCredentials(
                    serviceAccountCredentials,
                    projectId,
                    clientEmail);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load private key for clientEmail " + clientEmail, e);
        }
    }

    @Override
    public String info() {
        return "PKCS8 key for clientEmail " + clientEmail;
    }
}
