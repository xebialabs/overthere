package com.xebialabs.overthere.gcp.credentials;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.common.io.CharSource;

/**
 * Returns credentials defined by a Service Account key file in JSON format from the Google Developers Console.
 */
class ServiceAccountJsonGcpCredentialFactory extends GcpCredentialFactory {

    private final String jsonContent;

    ServiceAccountJsonGcpCredentialFactory(final String jsonContent) {
        this.jsonContent = jsonContent;
    }

    @Override
    protected ProjectCredentials doCreate() {
        try {
            ServiceAccountCredentials serviceAccountCredentials = ServiceAccountCredentials.fromStream(
                    CharSource.wrap(jsonContent).asByteSource(StandardCharsets.UTF_8).openStream()
            );
            return new ProjectCredentials(
                    serviceAccountCredentials,
                    serviceAccountCredentials.getProjectId(),
                    serviceAccountCredentials.getClientEmail());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot use credentials from " + jsonContent, e);
        }
    }

    @Override
    public String info() {
        return "credentials JSON content " + jsonContent;
    }
}
