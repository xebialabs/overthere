package com.xebialabs.overthere.gcp.credentials;

import com.google.auth.Credentials;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public final class ProjectCredentials {
    private Credentials credentials;
    private GoogleCredential googleCredential;
    private final String projectId;
    private final String clientEmail;

    ProjectCredentials(final Credentials credentials, final String projectId, final String clientEmail) {
        this.credentials = credentials;
        this.projectId = projectId;
        this.clientEmail = clientEmail;
    }

    ProjectCredentials(final GoogleCredential credentials, final String projectId, final String clientEmail) {
        this.googleCredential = credentials;
        this.projectId = projectId;
        this.clientEmail = clientEmail;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public GoogleCredential getGoogleCredentials() {
        return googleCredential;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getClientEmail() {
        return clientEmail;
    }
}
