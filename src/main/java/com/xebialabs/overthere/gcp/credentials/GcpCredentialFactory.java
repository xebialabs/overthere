package com.xebialabs.overthere.gcp.credentials;

import java.util.Collections;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;

import com.xebialabs.overthere.ConnectionOptions;

import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.*;

public abstract class GcpCredentialFactory {

    public ProjectCredentials withScope(ProjectCredentials projectCredentials) {
        Credentials credentials = projectCredentials.getCredentials();
        if (credentials instanceof GoogleCredentials) {
            GoogleCredentials GcpCredentialFactory = (GoogleCredentials) credentials;
            if (GcpCredentialFactory.createScopedRequired()) {
                return new ProjectCredentials(
                        GcpCredentialFactory.createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")),
                        projectCredentials.getProjectId(),
                        projectCredentials.getClientEmail());
            }
        }
        return projectCredentials;
    }

    public ProjectCredentials create() {
        ProjectCredentials credentials = doCreate();
        return withScope(credentials);
    }

    protected abstract ProjectCredentials doCreate();

    public abstract String info();

    public static GcpCredentialFactory getFactory(final ConnectionOptions options) {
        String credentialsFile = options.get(CREDENTIALS_FILE);
        String projectId = options.get(PROJECT_ID);
        String clientEmail = options.get(CLIENT_EMAIL);
        String instanceId = options.get(INSTANCE_ID);

        return new DefaultCredentialsGcpCredentialFactory(
               projectId, clientEmail
        );
    }

}
