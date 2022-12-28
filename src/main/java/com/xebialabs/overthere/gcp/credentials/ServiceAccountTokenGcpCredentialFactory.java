package com.xebialabs.overthere.gcp.credentials;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.OAuth2Credentials;

/**
 * Returns credentials defined by a Service Account key in JSON format from the Google Developers Console.
 */
class ServiceAccountTokenGcpCredentialFactory extends GcpCredentialFactory {

    private final String projectId;
    private final String apiToken;
    private final String clientEmail;
    private static final JsonFactory GSON_FACTORY = GsonFactory.getDefaultInstance();
    private  HttpTransport httpTransport;
    private Credential oauth2Credential;

    ServiceAccountTokenGcpCredentialFactory(final String projectId, final String apiToken, final String clientEmail) {
        this.projectId = projectId;
        this.apiToken = apiToken;
        this.clientEmail = clientEmail;
    }

    @Override
    protected ProjectCredentials doCreate() {
        try {
            AccessToken accessToken = new AccessToken(apiToken,null);
            OAuth2Credentials oAuth2Credentials = OAuth2Credentials.create(accessToken);
            this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            return new ProjectCredentials(
                    oAuth2Credentials,
                    this.projectId,
                    this.clientEmail);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Cannot use credentials from Token : %s", apiToken), e);
        }
    }

    @Override
    public String info() {
        return String.format("credentials Api Token : %s", apiToken);
    }
}
