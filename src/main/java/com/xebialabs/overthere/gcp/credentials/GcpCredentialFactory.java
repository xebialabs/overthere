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
        System.out.print("CAME 555");
        Object credentials = projectCredentials.getCredentials();
        System.out.print("CAME 666");
        if (credentials instanceof GoogleCredentials) {
            System.out.print("CAME 7777");
            GoogleCredentials gcpCredentialFactory = (GoogleCredentials) credentials;
            System.out.print("CAME 8888");
            if (gcpCredentialFactory.createScopedRequired()) {
                System.out.print("CAME 9999");
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
        System.out.print("CAME 444");
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
