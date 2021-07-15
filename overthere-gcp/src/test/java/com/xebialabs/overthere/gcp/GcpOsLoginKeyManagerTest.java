package com.xebialabs.overthere.gcp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.google.cloud.oslogin.common.OsLoginProto;
import com.google.cloud.oslogin.v1.LoginProfile;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialFactory;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialsType;

import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcpOsLoginKeyManagerTest {

    private final GenerateSshKey generateSshKey = new JCraftGenerateSshKey();
    private GcpCredentialFactory gcpCredentialFactory;

    @BeforeClass
    public void init() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(CREDENTIALS_FILE, Utils.getClasspathFile("gcp/sa-key-ssh-account.json"));
        gcpCredentialFactory = GcpCredentialsType.ServiceAccountJsonFile.createGcpCredentialFactory(options);
    }

    @Test
    public void canGenerateNewKey() {
        final long expiryTime = 100_000;
        final long epochExpiryTime = expiryTime * 1000 + System.currentTimeMillis() * 1000;

        final GcpOsLoginKeyManager gcpOsLoginKeyManager = new GcpOsLoginKeyManager(generateSshKey, gcpCredentialFactory) {
            @Override
            protected LoginProfile importSssKeyProjectLevel(final SshKeyPair sshKeyPair, final long expiryInUsec) {
                return LoginProfile.getDefaultInstance().toBuilder()
                        .putSshPublicKeys(
                                sshKeyPair.getFingerPrint(),
                                OsLoginProto.SshPublicKey.getDefaultInstance().toBuilder()
                                        .setKey(sshKeyPair.getPublicKey())
                                        .setFingerprint(sshKeyPair.getFingerPrint())
                                        .setExpirationTimeUsec(epochExpiryTime)
                                        .build()
                        )
                        .addPosixAccounts(OsLoginProto.PosixAccount.getDefaultInstance()
                                .toBuilder()
                                .setAccountId("testAccount")
                                .setUsername("username")
                                .build())
                        .build();
            }
        };

        gcpOsLoginKeyManager.init();
        GcpSshKey gcpSshKey = gcpOsLoginKeyManager.refreshKey(expiryTime, 1024);

        assertThat(gcpSshKey.getPrivateKey(), notNullValue());
        assertThat(gcpSshKey.getExpirationTimeMs(), equalTo(epochExpiryTime / 1000));
    }

    @Test
    public void canReuseKeyWithingExpiryPeriod() {
        final long expiryTime = 100_000;
        final long epochExpiryTime = expiryTime * 1000 + System.currentTimeMillis() * 1000;

        final GcpOsLoginKeyManager gcpOsLoginKeyManager = new GcpOsLoginKeyManager(generateSshKey, gcpCredentialFactory) {
            @Override
            protected LoginProfile importSssKeyProjectLevel(final SshKeyPair sshKeyPair, final long expiryInUsec) {
                return LoginProfile.getDefaultInstance().toBuilder()
                        .putSshPublicKeys(
                                sshKeyPair.getFingerPrint(),
                                OsLoginProto.SshPublicKey.getDefaultInstance().toBuilder()
                                        .setKey(sshKeyPair.getPublicKey())
                                        .setFingerprint(sshKeyPair.getFingerPrint())
                                        .setExpirationTimeUsec(epochExpiryTime)
                                        .build()
                        )
                        .addPosixAccounts(OsLoginProto.PosixAccount.getDefaultInstance()
                                .toBuilder()
                                .setAccountId("testAccount")
                                .setUsername("username")
                                .build())
                        .build();
            }
        };

        gcpOsLoginKeyManager.init();
        GcpSshKey gcpSshKey1 = gcpOsLoginKeyManager.refreshKey(expiryTime, 1024);

        assertThat(gcpSshKey1.getPrivateKey(), notNullValue());
        assertThat(gcpSshKey1.getExpirationTimeMs(), equalTo(epochExpiryTime / 1000));

        GcpSshKey gcpSshKey2 = gcpOsLoginKeyManager.refreshKey(expiryTime, 1024);

        assertThat(gcpSshKey2.getPrivateKey(), equalTo(gcpSshKey1.getPrivateKey()));
        assertThat(gcpSshKey2.getExpirationTimeMs(), equalTo(epochExpiryTime / 1000));
    }

    @Test
    public void canGenerateNewKeyInCaseOfExpiry() {
        final long expiryTime = 999;

        final GcpOsLoginKeyManager gcpOsLoginKeyManager = new GcpOsLoginKeyManager(generateSshKey, gcpCredentialFactory) {
            @Override
            protected LoginProfile importSssKeyProjectLevel(final SshKeyPair sshKeyPair, final long expiryInUsec) {
                return LoginProfile.getDefaultInstance().toBuilder()
                        .putSshPublicKeys(
                                sshKeyPair.getFingerPrint(),
                                OsLoginProto.SshPublicKey.getDefaultInstance().toBuilder()
                                        .setKey(sshKeyPair.getPublicKey())
                                        .setFingerprint(sshKeyPair.getFingerPrint())
                                        .setExpirationTimeUsec(expiryTime * 1000 + System.currentTimeMillis() * 1000)
                                        .build()
                        )
                        .addPosixAccounts(OsLoginProto.PosixAccount.getDefaultInstance()
                                .toBuilder()
                                .setAccountId("testAccount")
                                .setUsername("username")
                                .build())
                        .build();
            }
        };

        gcpOsLoginKeyManager.init();
        GcpSshKey gcpSshKey1 = gcpOsLoginKeyManager.refreshKey(expiryTime, 1024);

        assertThat(gcpSshKey1.getPrivateKey(), notNullValue());

        GcpSshKey gcpSshKey2 = gcpOsLoginKeyManager.refreshKey(expiryTime, 1024);

        assertThat(gcpSshKey2.getPrivateKey(), not(gcpSshKey1.getPrivateKey()));
    }
}
