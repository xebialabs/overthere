package com.xebialabs.overthere.gcp.credentials;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.util.Base64;
import org.testng.annotations.Test;
import com.google.auth.oauth2.ServiceAccountCredentials;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Utils;

import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CLIENT_EMAIL;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CLIENT_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_JSON;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_CREDENTIALS_TYPE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.PRIVATE_KEY_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.PRIVATE_KEY_PKCS8;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.PROJECT_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.SCOPES;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.SERVICE_ACCOUNT_USER;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.TOKEN_SERVER_URI;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcpCredentialsTypeTest {

    private final ConnectionOptions options = new ConnectionOptions();

    @Test
    public void canSetupDefaultGcpCredentialsType() {
        options.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.Default.name());
        options.set(PROJECT_ID, "project_id");
        options.set(CLIENT_EMAIL, "client@test.com");

        GcpCredentialsType resolved = GcpCredentialsType.resolve(options);
        String key = resolved.createKey(options);
        GcpCredentialFactory gcpCredentialFactory = resolved.createGcpCredentialFactory(options);

        assertThat(resolved, equalTo(GcpCredentialsType.Default));
        assertThat(key, startsWith(GcpCredentialsType.Default.name()));
        assertThat(gcpCredentialFactory instanceof DefaultCredentialsGcpCredentialFactory, equalTo(true));
    }

    @Test
    public void canSetupServiceAccountJsonFileGcpCredentialsType() throws Exception {
        options.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        options.set(CREDENTIALS_FILE, Utils.getClasspathFile("gcp/sa-key-ssh-account.json"));

        GcpCredentialsType resolved = GcpCredentialsType.resolve(options);
        String key = resolved.createKey(options);
        GcpCredentialFactory gcpCredentialFactory = resolved.createGcpCredentialFactory(options);

        assertThat(resolved, equalTo(GcpCredentialsType.ServiceAccountJsonFile));
        assertThat(key, startsWith(GcpCredentialsType.ServiceAccountJsonFile.name()));
        assertThat(gcpCredentialFactory instanceof ServiceAccountFileGcpCredentialFactory, equalTo(true));
    }

    @Test
    public void canSetupServiceAccountJsonGcpCredentialsType() throws Exception {
        String serviceAccountObjectFile = Utils.getClasspathFile("gcp/sa-key-ssh-account.json");
        options.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJson.name());
        options.set(CREDENTIALS_JSON, FileUtils.readFileToString(new File(serviceAccountObjectFile)));

        GcpCredentialsType resolved = GcpCredentialsType.resolve(options);
        String key = resolved.createKey(options);
        GcpCredentialFactory gcpCredentialFactory = resolved.createGcpCredentialFactory(options);

        assertThat(resolved, equalTo(GcpCredentialsType.ServiceAccountJson));
        assertThat(key, startsWith(GcpCredentialsType.ServiceAccountJson.name()));
        assertThat(gcpCredentialFactory instanceof ServiceAccountJsonGcpCredentialFactory, equalTo(true));
    }

    @Test
    public void canSetupServiceAccountPkcs8GcpCredentialsType() throws Exception {
        ServiceAccountCredentials serviceAccountCredentials =
                ServiceAccountCredentials.fromStream(new FileInputStream(Utils.getClasspathFile("gcp/sa-key-ssh-account.json")));
        options.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountPkcs8.name());
        options.set(CLIENT_ID, serviceAccountCredentials.getClientId());
        options.set(CLIENT_EMAIL, serviceAccountCredentials.getClientEmail());
        options.set(PRIVATE_KEY_ID, serviceAccountCredentials.getPrivateKeyId());
        options.set(PRIVATE_KEY_PKCS8, Base64.encodeBase64String(serviceAccountCredentials.getPrivateKey().getEncoded()));
        options.set(SCOPES, joinToString(serviceAccountCredentials.getScopes()));
        options.set(TOKEN_SERVER_URI, serviceAccountCredentials.getTokenServerUri().toString());
        options.set(SERVICE_ACCOUNT_USER, serviceAccountCredentials.getServiceAccountUser());

        GcpCredentialsType resolved = GcpCredentialsType.resolve(options);
        String key = resolved.createKey(options);
        GcpCredentialFactory gcpCredentialFactory = resolved.createGcpCredentialFactory(options);

        assertThat(resolved, equalTo(GcpCredentialsType.ServiceAccountPkcs8));
        assertThat(key, startsWith(GcpCredentialsType.ServiceAccountPkcs8.name()));
        assertThat(gcpCredentialFactory instanceof ServiceAccountPkcs8GcpCredentialFactory, equalTo(true));
    }

    private static String joinToString(Collection<String> collection) {
        StringBuilder csvBuilder = new StringBuilder();
        for (String entry : collection) {
            csvBuilder.append(entry);
            csvBuilder.append(',');
        }
        return csvBuilder.toString();
    }
}
