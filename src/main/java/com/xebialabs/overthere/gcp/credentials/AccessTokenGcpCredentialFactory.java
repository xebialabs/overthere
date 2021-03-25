package com.xebialabs.overthere.gcp.credentials;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;

class AccessTokenGcpCredentialFactory extends GcpCredentialFactory {

    private final com.google.auth.oauth2.AccessToken accessToken;
    private final String projectId;
    private final String clientEmail;

    AccessTokenGcpCredentialFactory(final AccessToken accessToken, final String projectId, final String clientEmail) {
        this.accessToken = accessToken;
        this.projectId = projectId;
        this.clientEmail = clientEmail;
    }

    @Override
    public ProjectCredentials doCreate() {
        return new ProjectCredentials(OAuth2Credentials.create(accessToken), projectId, clientEmail);
    }

    @Override
    public String info() {
        return "access token for clientEmail " + clientEmail;
    }
}
