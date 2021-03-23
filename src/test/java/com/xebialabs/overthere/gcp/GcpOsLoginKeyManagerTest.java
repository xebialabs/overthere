package com.xebialabs.overthere.gcp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.google.cloud.oslogin.common.OsLoginProto;
import com.google.cloud.oslogin.v1.LoginProfile;
import com.google.common.io.Resources;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcpOsLoginKeyManagerTest {

    private String credFile;
    private GenerateSshKey generateSshKey = new JCraftGenerateSshKey();

    @BeforeClass
    public void init() throws Exception {
        credFile = Resources.getResource("gcp/sa-key-ssh-account.json").getFile();
    }

    @Test
    public void canGenerateNewKey() {
        final long expiryTime = 100_000_000;
        final long epochExpiryTime = expiryTime + System.currentTimeMillis() * 1000;

        final GcpOsLoginKeyManager gcpOsLoginKeyManager = new GcpOsLoginKeyManager(generateSshKey, credFile) {
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
        assertThat(gcpSshKey.getExpirationTimeUsec(), equalTo(epochExpiryTime));
    }

    @Test
    public void canReuseKeyWithingExpiryPeriod() {
        final long expiryTime = 100_000_000;
        final long epochExpiryTime = expiryTime + System.currentTimeMillis() * 1000;

        final GcpOsLoginKeyManager gcpOsLoginKeyManager = new GcpOsLoginKeyManager(generateSshKey, credFile) {
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
        assertThat(gcpSshKey1.getExpirationTimeUsec(), equalTo(epochExpiryTime));

        GcpSshKey gcpSshKey2 = gcpOsLoginKeyManager.refreshKey(expiryTime, 1024);

        assertThat(gcpSshKey2.getPrivateKey(), equalTo(gcpSshKey1.getPrivateKey()));
        assertThat(gcpSshKey2.getExpirationTimeUsec(), equalTo(epochExpiryTime));
    }

    @Test
    public void canGenerateNewKeyInCaseOfExpiry() {
        final long expiryTime = 999;

        final GcpOsLoginKeyManager gcpOsLoginKeyManager = new GcpOsLoginKeyManager(generateSshKey, credFile) {
            @Override
            protected LoginProfile importSssKeyProjectLevel(final SshKeyPair sshKeyPair, final long expiryInUsec) {
                return LoginProfile.getDefaultInstance().toBuilder()
                        .putSshPublicKeys(
                                sshKeyPair.getFingerPrint(),
                                OsLoginProto.SshPublicKey.getDefaultInstance().toBuilder()
                                        .setKey(sshKeyPair.getPublicKey())
                                        .setFingerprint(sshKeyPair.getFingerPrint())
                                        .setExpirationTimeUsec(expiryTime + System.currentTimeMillis() * 1000)
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
