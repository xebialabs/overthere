package com.xebialabs.overthere.gcp.credentials;

import java.io.IOException;
import com.google.auth.oauth2.GoogleCredentials;

class DefaultCredentialsGcpCredentialFactory extends GcpCredentialFactory {

    private final String projectId;
    private final String clientEmail;

    DefaultCredentialsGcpCredentialFactory(final String projectId, final String clientEmail) {
        this.projectId = projectId;
        this.clientEmail = clientEmail;
    }

    @Override
    public ProjectCredentials doCreate() {
        try {
            return new ProjectCredentials(GoogleCredentials.getApplicationDefault(), projectId, clientEmail);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load default credentials", e);
        }
    }

    @Override
    public String info() {
        return "access token for clientEmail " + clientEmail;
    }
}
