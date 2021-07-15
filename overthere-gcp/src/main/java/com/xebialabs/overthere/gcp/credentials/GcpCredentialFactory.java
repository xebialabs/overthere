package com.xebialabs.overthere.gcp.credentials;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;

import java.util.Collections;

/**
 * Abstract factory that base for implementation of credential factory.
 * Credential factory produces credentials that will be used by GCP API for secure management through GCP's APIs.
 */
public abstract class GcpCredentialFactory {

    /**
     * Adds additional scopes that are used with credentials
     *
     * @param projectCredentials current credentials
     * @return credentials with additional scopes.
     */
    public ProjectCredentials withScope(ProjectCredentials projectCredentials) {
        Credentials credentials = projectCredentials.getCredentials();
        if (credentials instanceof GoogleCredentials) {
            GoogleCredentials gcpCredentialFactory = (GoogleCredentials) credentials;
            if (gcpCredentialFactory.createScopedRequired()) {
                return new ProjectCredentials(
                        gcpCredentialFactory.createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform")),
                        projectCredentials.getProjectId(),
                        projectCredentials.getClientEmail());
            }
        }
        return projectCredentials;
    }

    /**
     * Creates project credentials based on custom implementation and options defined for each instance.
     *
     * @return new project credentials.
     */
    public ProjectCredentials create() {
        ProjectCredentials credentials = doCreate();
        return withScope(credentials);
    }

    protected abstract ProjectCredentials doCreate();

    /**
     * Returns string that will be used for logging to describe this factory.
     *
     * @return factory string
     */
    public abstract String info();

}
