package com.xebialabs.overthere.gcp.credentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.google.auth.oauth2.ServiceAccountCredentials;

class ServiceAccountFileGcpCredentialFactory extends GcpCredentialFactory {

    private final File credentialsFile;

    ServiceAccountFileGcpCredentialFactory(final File credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    @Override
    public ProjectCredentials doCreate() {
        try {
            ServiceAccountCredentials serviceAccountCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(credentialsFile));
            return new ProjectCredentials(
                    serviceAccountCredentials,
                    serviceAccountCredentials.getProjectId(),
                    serviceAccountCredentials.getClientEmail());
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load credentials file " + credentialsFile.getAbsolutePath(), e);
        }
    }

    @Override
    public String info() {
        return "credentials file " + credentialsFile.getAbsolutePath();
    }
}
