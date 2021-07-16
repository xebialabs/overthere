package com.xebialabs.overthere.gcp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.api.services.compute.model.Metadata;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialFactory;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialsType;

import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcpMetadataKeyManagerTest {

    private static final String TEST_SSH_KEY =
            "someUsername:PUBLIC_KEY {\"userName\":\"someUsername\",\"expireOn\":\"1970-01-01T00:20:34+0000\"}";

    private final GenerateSshKey generateSshKey = new JCraftGenerateSshKey();
    private GcpCredentialFactory gcpCredentialFactory;
    private GcpMetadataKeyManager gcpMetadataKeyManager;

    private final String zoneName = "someZone";
    private final String instanceId = "someInstanceId";
    private final String username = "someUsername";
    private final String applicationName = "someApplicationName";

    @BeforeClass
    public void init() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(CREDENTIALS_FILE, Utils.getClasspathFile("gcp/sa-key-ssh-account.json"));
        gcpCredentialFactory = GcpCredentialsType.ServiceAccountJsonFile.createGcpCredentialFactory(options);
        gcpMetadataKeyManager = new GcpMetadataKeyManager(
                generateSshKey, gcpCredentialFactory, zoneName, instanceId, username, applicationName);
    }

    @Test
    public void canComposeSShKeyLine() {
        String resultPublicKey = gcpMetadataKeyManager.composeSshKeyLine("PUBLIC_KEY", 1234567);

        assertThat(resultPublicKey, equalTo(TEST_SSH_KEY));
    }

    @Test
    public void canComposeFirstSshKeyItem() {
        Metadata.Items resultSshKeys = gcpMetadataKeyManager.composeSshKeyItem(null, "PUBLIC_KEY", 1234567);

        assertThat(resultSshKeys.getKey(), equalTo("ssh-keys"));
        assertThat(resultSshKeys.getValue(), equalTo(TEST_SSH_KEY));
    }

    @Test
    public void canReplaceFirstSshKeyItem() {
        Metadata.Items resultSshKeys = gcpMetadataKeyManager.composeSshKeyItem(TEST_SSH_KEY.replace("PUBLIC_KEY", "OLD_PUBLIC_KEY"), "PUBLIC_KEY", 1234567);

        assertThat(resultSshKeys.getKey(), equalTo("ssh-keys"));
        assertThat(resultSshKeys.getValue(), equalTo(TEST_SSH_KEY));
    }

    @Test
    public void canAddNewSshKeyItem() {
        StringBuilder stringBuilder = new StringBuilder();

        Metadata.Items resultSshKeys = gcpMetadataKeyManager.composeSshKeyItem(TEST_SSH_KEY.replace("PUBLIC_KEY", "OLD_PUBLIC_KEY"), "PUBLIC_KEY", 1234567);

        assertThat(resultSshKeys.getKey(), equalTo("ssh-keys"));
        assertThat(resultSshKeys.getValue(), equalTo(TEST_SSH_KEY));
    }
}
