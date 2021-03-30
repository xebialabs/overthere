package com.xebialabs.overthere.gcp;

import java.util.concurrent.atomic.AtomicInteger;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialsType;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.userauth.UserAuthException;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_CREDENTIALS_TYPE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_KEY_MANAGEMENT_TYPE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.KEY_EXPIRY_TIME_MILLIS;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.KEY_SIZE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.RETRY_COUNT;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.RETRY_PERIOD_MILLIS;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.ZONE_NAME;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

public class GcpSshConnectionBuilderTest {

    private String credFile;
    @Mock
    private GcpKeyManager gcpKeyManager;

    @BeforeClass
    public void init() throws Exception {
        MockitoAnnotations.initMocks(this);
        credFile = Utils.getClasspathFile("gcp/sa-key-ssh-account.json");
        when(gcpKeyManager.refreshKey(anyLong(), anyInt()))
                .thenReturn(new GcpSshKey(new SshKeyPair("", "", "", ""), "", 0));
    }

    @Test
    public void defaultOptionsValuesAreSet() {

        ConnectionOptions connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SCP);
        connectionOptions.set(OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
        connectionOptions.set(ADDRESS, "localhost");
        connectionOptions.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        connectionOptions.set(CREDENTIALS_FILE, credFile);
        connectionOptions.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.OsLogin.name());

        GcpSshConnectionBuilder gcpSshConnectionBuilder = new GcpSshConnectionBuilder(
                GcpSshConnectionBuilder.GCP_SSH_PROTOCOL, connectionOptions, DefaultAddressPortMapper.INSTANCE);

        assertThat(gcpSshConnectionBuilder.getKeyExpiryTimeMillis(), equalTo(300_000L));
        assertThat(gcpSshConnectionBuilder.getKeySize(), equalTo(1024));
        assertThat(gcpSshConnectionBuilder.getRetryCount(), equalTo(3));
        assertThat(gcpSshConnectionBuilder.getRetryPeriodMillis(), equalTo(1000));
    }

    @Test
    public void customOptionsValuesAreSet() {

        ConnectionOptions connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SCP);
        connectionOptions.set(OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
        connectionOptions.set(ADDRESS, "localhost");
        connectionOptions.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        connectionOptions.set(CREDENTIALS_FILE, credFile);
        connectionOptions.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.OsLogin.name());
        connectionOptions.set(KEY_EXPIRY_TIME_MILLIS, 100_000);
        connectionOptions.set(KEY_SIZE, 2048);
        connectionOptions.set(RETRY_COUNT, 2);
        connectionOptions.set(RETRY_PERIOD_MILLIS, 100);

        GcpSshConnectionBuilder gcpSshConnectionBuilder = new GcpSshConnectionBuilder(
                GcpSshConnectionBuilder.GCP_SSH_PROTOCOL, connectionOptions, DefaultAddressPortMapper.INSTANCE);

        assertThat(gcpSshConnectionBuilder.getKeyExpiryTimeMillis(), equalTo(100_000L));
        assertThat(gcpSshConnectionBuilder.getKeySize(), equalTo(2048));
        assertThat(gcpSshConnectionBuilder.getRetryCount(), equalTo(2));
        assertThat(gcpSshConnectionBuilder.getRetryPeriodMillis(), equalTo(100));
    }

    @Test
    public void canRetryConnectionOnUserAuthException() {

        final String exceptionMessage = "Connection failed";
        final int retryCount = 5;

        final AtomicInteger counter = new AtomicInteger(0);
        ConnectionOptions connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SCP);
        connectionOptions.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        connectionOptions.set(CREDENTIALS_FILE, credFile);
        connectionOptions.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.Metadata.name());
        connectionOptions.set(ZONE_NAME, "test-zone");
        connectionOptions.set(USERNAME, "overthere_user");
        connectionOptions.set(OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
        connectionOptions.set(ADDRESS, "localhost");
        connectionOptions.set(RETRY_COUNT, retryCount);
        connectionOptions.set(RETRY_PERIOD_MILLIS, 100);

        GcpSshConnectionBuilder gcpSshConnectionBuilder = new GcpSshConnectionBuilder(
                GcpSshConnectionBuilder.GCP_SSH_PROTOCOL, connectionOptions, DefaultAddressPortMapper.INSTANCE) {
            @Override
            protected OverthereConnection tryToConnect() {
                counter.incrementAndGet();
                throw new RuntimeIOException(
                        new UserAuthException(DisconnectReason.NO_MORE_AUTH_METHODS_AVAILABLE, exceptionMessage)
                );
            }
        };
        gcpSshConnectionBuilder.setGcpKeyManager(gcpKeyManager);

        try {
            gcpSshConnectionBuilder.connect();
            Assert.fail("RuntimeIOException expected.");
        } catch (RuntimeIOException e) {
            e.printStackTrace();
            assertThat(e.getCause() instanceof UserAuthException, equalTo(true));
            assertThat(e.getCause().getMessage(), equalTo(exceptionMessage));
            assertThat(counter.get(), equalTo(retryCount));
        }
    }

    @Test
    public void canImmediatelyFailOnAnyException() {

        final String exceptionMessage = "Connection failed";

        final AtomicInteger counter = new AtomicInteger(0);
        ConnectionOptions connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SCP);
        connectionOptions.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        connectionOptions.set(CREDENTIALS_FILE, credFile);
        connectionOptions.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.Metadata.name());
        connectionOptions.set(ZONE_NAME, "test-zone");
        connectionOptions.set(USERNAME, "overthere_user");
        connectionOptions.set(OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
        connectionOptions.set(ADDRESS, "localhost");

        GcpSshConnectionBuilder gcpSshConnectionBuilder = new GcpSshConnectionBuilder(
                GcpSshConnectionBuilder.GCP_SSH_PROTOCOL, connectionOptions, DefaultAddressPortMapper.INSTANCE) {
            @Override
            protected OverthereConnection tryToConnect() {
                counter.incrementAndGet();
                throw new RuntimeIOException(exceptionMessage);
            }
        };
        gcpSshConnectionBuilder.setGcpKeyManager(gcpKeyManager);

        try {
            gcpSshConnectionBuilder.connect();
            Assert.fail("RuntimeIOException expected.");
        } catch (RuntimeIOException e) {
            assertThat(counter.get(), equalTo(1));
            assertThat(e.getMessage(), equalTo(exceptionMessage));
        } catch (Exception e) {
            Assert.fail("RuntimeIOException expected instead have " + e.getClass(), e);
        }
    }

}
