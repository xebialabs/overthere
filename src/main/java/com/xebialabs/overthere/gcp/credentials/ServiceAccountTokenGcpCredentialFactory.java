package com.xebialabs.overthere.gcp.credentials;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;

import java.io.IOException;
import java.security.GeneralSecurityException;
/**
 * Returns credentials defined by a Service Account key in JSON format from the Google Developers Console.
 */
class ServiceAccountTokenGcpCredentialFactory extends GcpCredentialFactory {

    private final String projectId;
    private final String apiToken;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private  HttpTransport httpTransport;

    ServiceAccountTokenGcpCredentialFactory(final String projectId, final String apiToken) {
        this.projectId = projectId;
        this.apiToken = apiToken;
    }

    @Override
    protected ProjectCredentials doCreate() {
        try {
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken(apiToken);
            return new ProjectCredentials(new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(
                    httpTransport)
                    .setJsonFactory(JSON_FACTORY)
                    .setTokenServerUrl(
                            new GenericUrl("https://www.googleapis.com/auth/cloud-platform"))
                    .build()
                    .setFromTokenResponse(tokenResponse), projectId, "");
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalArgumentException(String.format("Cannot use credentials from Token : %s", apiToken), e);
        }
    }

    @Override
    public String info() {
        return String.format("credentials Api Token : %s", apiToken);
    }
}
