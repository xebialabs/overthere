package com.xebialabs.overthere.gcp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.oslogin.common.OsLoginProto;
import com.google.cloud.oslogin.v1.*;

public class GcpOsLoginKeyManager implements GcpKeyManager {

    private final File credentialsFile;
    private final GenerateSshKey generateSshKey;
    private ServiceAccountCredentials serviceAccountCredentials;
    private UserName userName;
    private OsLoginServiceSettings osLoginServiceSettings;
    private GcpSshKey gcpSshKey;

    public GcpOsLoginKeyManager(final GenerateSshKey generateSshKey, final String credentialsFile) {
        this.generateSshKey = generateSshKey;
        this.credentialsFile = new File(credentialsFile);
    }

    @Override
    public GcpKeyManager init() {
        try {
            serviceAccountCredentials = ServiceAccountCredentials.fromStream(new FileInputStream(credentialsFile));
            userName = UserName.of(serviceAccountCredentials.getClientEmail());
            osLoginServiceSettings =
                    OsLoginServiceSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(serviceAccountCredentials))
                            .build();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot load credentials file " + credentialsFile.getAbsolutePath(), e);
        }
        return this;
    }

    @Override
    public GcpSshKey refreshKey(long expiryInUsec, int keySize) {
        // check if key valid for next second
        if (gcpSshKey == null || System.currentTimeMillis() + 1_000 > this.gcpSshKey.getExpirationTimeUsec() / 1_000) {

            SshKeyPair sshKeyPair = generateSshKey.generate(this.serviceAccountCredentials.getClientEmail(), keySize);
            long expirationTimeUsec = System.currentTimeMillis() * 1000 + expiryInUsec;
            LoginProfile loginProfile = importSssKeyProjectLevel(sshKeyPair, expiryInUsec);
            int posixAccountsCount = loginProfile.getPosixAccountsCount();
            if (posixAccountsCount < 1) {
                throw new IllegalArgumentException("Service account from file " + credentialsFile.getAbsolutePath() + " has no posix account");
            }
            OsLoginProto.PosixAccount posixAccount = loginProfile.getPosixAccounts(0);

            Map<String, OsLoginProto.SshPublicKey> sshPublicKeysMap = loginProfile.getSshPublicKeysMap();
            OsLoginProto.SshPublicKey sshPublicKey = sshPublicKeysMap.get(sshKeyPair.getFingerPrint());
            if (sshPublicKey != null) {
                expirationTimeUsec = sshPublicKey.getExpirationTimeUsec();
            }

            this.gcpSshKey = new GcpSshKey(sshKeyPair, posixAccount.getUsername(), expirationTimeUsec);
        }
        return this.gcpSshKey;
    }

    protected LoginProfile importSssKeyProjectLevel(SshKeyPair sshKeyPair, long expiryInUsec) {
        try (OsLoginServiceClient osLoginServiceClient = OsLoginServiceClient.create(osLoginServiceSettings)) {
            OsLoginProto.SshPublicKey sshPublicKey = createSshPublicKey(sshKeyPair, expiryInUsec);
            return osLoginServiceClient.importSshPublicKey(userName, sshPublicKey, serviceAccountCredentials.getProjectId())
                    .getLoginProfile();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot use credentials from file " + credentialsFile.getAbsolutePath(), e);
        }
    }

    private OsLoginProto.SshPublicKey createSshPublicKey(SshKeyPair sshKeyPair, long expiryInUsec) {
        return OsLoginProto.SshPublicKey.newBuilder()
                .setKey(sshKeyPair.getPublicKey())
                .setExpirationTimeUsec(System.currentTimeMillis() * 1_000 + expiryInUsec)
                .build();
    }
}
