package com.xebialabs.overthere.gcp.credentials;

import com.google.auth.Credentials;

public class ProjectCredentials {
    private final Credentials credentials;
    private final String projectId;
    private final String clientEmail;

    ProjectCredentials(final Credentials credentials, final String projectId, final String clientEmail) {
        this.credentials = credentials;
        this.projectId = projectId;
        this.clientEmail = clientEmail;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getClientEmail() {
        return clientEmail;
    }
}
