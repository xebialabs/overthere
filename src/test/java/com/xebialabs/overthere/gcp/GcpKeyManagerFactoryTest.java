package com.xebialabs.overthere.gcp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Utils;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialsType;

import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_CREDENTIALS_TYPE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_KEY_MANAGEMENT_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcpKeyManagerFactoryTest {

    private String credFile1;
    private String credFile2;

    @BeforeClass
    public void init() throws Exception {
        credFile1 = Utils.getClasspathFile("gcp/sa-key-ssh-account.json");
        credFile2 = Utils.getClasspathFile("gcp/sa-key-ssh-account2.json");
    }

    @Test
    public void canReuseNewGcpKeyManager() {
        ConnectionOptions options1 = new ConnectionOptions();
        options1.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.OsLogin.name());
        options1.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        options1.set(CREDENTIALS_FILE, credFile1);

        ConnectionOptions options2 = new ConnectionOptions();
        options2.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.OsLogin.name());
        options2.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        options2.set(CREDENTIALS_FILE, credFile1);

        GcpKeyManager gcpKeyManager1 = GcpKeyManagerFactory.create(options1);
        GcpKeyManager gcpKeyManager2 = GcpKeyManagerFactory.create(options2);

        assertThat(gcpKeyManager1, notNullValue());
        assertThat(gcpKeyManager2, equalTo(gcpKeyManager1));
    }

    @Test
    public void canCreateNewGcpKeyManagerForNewKeyFile() {
        ConnectionOptions options1 = new ConnectionOptions();
        options1.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.OsLogin.name());
        options1.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        options1.set(CREDENTIALS_FILE, credFile1);

        ConnectionOptions options2 = new ConnectionOptions();
        options2.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.OsLogin.name());
        options2.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        options2.set(CREDENTIALS_FILE, credFile2);

        GcpKeyManager gcpKeyManager1 = GcpKeyManagerFactory.create(options1);
        GcpKeyManager gcpKeyManager2 = GcpKeyManagerFactory.create(options2);

        assertThat(gcpKeyManager1, notNullValue());
        assertThat(gcpKeyManager2, notNullValue());
        assertThat(gcpKeyManager2, not(gcpKeyManager1));
    }

}
