package com.xebialabs.overthere.gcp.credentials;

import java.io.IOException;
import com.google.auth.oauth2.GoogleCredentials;

/**
 * Creates the Application Default Credentials.
 *
 * <p>Creates the Application Default Credentials which are used to identify and authorize the
 * whole application. The following are searched (in order) to find the Application Default
 * Credentials:
 *
 * <ol>
 *   <li>Credentials file pointed to by the {@code GOOGLE_APPLICATION_CREDENTIALS} environment
 *       variable
 *   <li>Credentials provided by the Google Cloud SDK {@code gcloud auth application-default
 *       login} command
 *   <li>Google App Engine built-in credentials
 *   <li>Google Cloud Shell built-in credentials
 *   <li>Google Compute Engine built-in credentials
 * </ol>
 */
class DefaultCredentialsGcpCredentialFactory extends GcpCredentialFactory {

    private final String projectId;
    private final String clientEmail;

    DefaultCredentialsGcpCredentialFactory(final String projectId, final String clientEmail) {
        this.projectId = projectId;
        this.clientEmail = clientEmail;
    }

    @Override
    protected ProjectCredentials doCreate() {
        try {
            return new ProjectCredentials(GoogleCredentials.getApplicationDefault(), projectId, clientEmail);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load default credentials", e);
        }
    }

    @Override
    public String info() {
        return "default credentials for clientEmail " + clientEmail;
    }
}
