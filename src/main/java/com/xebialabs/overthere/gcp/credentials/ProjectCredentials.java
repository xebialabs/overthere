package com.xebialabs.overthere.gcp.credentials;

import com.google.auth.Credentials;
import com.google.api.client.auth.oauth2.Credential;
public final class ProjectCredentials {
    private Credentials credentials;
    private Credential credential;
    private final String projectId;
    private final String clientEmail;

    ProjectCredentials(final Credentials credentials, final String projectId, final String clientEmail) {
        this.credentials = credentials;
        this.projectId = projectId;
        this.clientEmail = clientEmail;
    }

    ProjectCredentials(final Credential credential, final String projectId, final String clientEmail) {
        this.credential = credential;
        this.projectId = projectId;
        this.clientEmail = clientEmail;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public Credential getCredential() {
        return credential;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getClientEmail() {
        return clientEmail;
    }
}
