package com.xebialabs.overthere.gcp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialFactory;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialsType;

import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.APPLICATION_NAME;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_KEY_MANAGEMENT_TYPE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.INSTANCE_ID;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.ZONE_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcpKeyManagementTypeTest {

    private final ConnectionOptions options = new ConnectionOptions();
    private GcpCredentialFactory gcpCredentialFactory;

    @BeforeClass
    public void init() throws Exception {
        options.set(CREDENTIALS_FILE, Utils.getClasspathFile("gcp/sa-key-ssh-account.json"));
        gcpCredentialFactory = GcpCredentialsType.ServiceAccountJsonFile.createGcpCredentialFactory(options);
    }

    @Test
    public void canSetupOsLoginKeyManager() {
        options.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.OsLogin.name());

        GcpKeyManager gcpKeyManager = GcpKeyManagementType.resolveGcpKeyManager(options, gcpCredentialFactory)
                .init();

        assertThat(gcpKeyManager instanceof GcpOsLoginKeyManager, equalTo(true));
    }

    @Test
    public void canSetupMetadataKeyManager() {
        options.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.Metadata.name());
        options.set(ZONE_NAME, "testZone");
        options.set(INSTANCE_ID, "instanceId");
        options.set(USERNAME, "test_username");
        options.set(APPLICATION_NAME, "test_app");

        GcpKeyManager gcpKeyManager = GcpKeyManagementType.resolveGcpKeyManager(options, gcpCredentialFactory)
                .init();

        assertThat(gcpKeyManager instanceof GcpMetadataKeyManager, equalTo(true));
        GcpMetadataKeyManager gcpMetadataKeyManager = (GcpMetadataKeyManager) gcpKeyManager;
        assertThat(gcpMetadataKeyManager.getZoneName(), equalTo("testZone"));
        assertThat(gcpMetadataKeyManager.getInstanceId(), equalTo("instanceId"));
        assertThat(gcpMetadataKeyManager.getUsername(), equalTo("test_username"));
        assertThat(gcpMetadataKeyManager.getApplicationName(), equalTo("test_app"));
    }
}
