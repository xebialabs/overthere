package com.xebialabs.overthere.gcp;

import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.cloud.oslogin.common.OsLoginProto;
import com.google.cloud.oslogin.v1.*;

import com.xebialabs.overthere.gcp.credentials.GcpCredentialFactory;
import com.xebialabs.overthere.gcp.credentials.ProjectCredentials;

/**
 *
 */
public class GcpOsLoginKeyManager implements GcpKeyManager {
    private static final Logger logger = LoggerFactory.getLogger(GcpOsLoginKeyManager.class);

    private final GcpCredentialFactory gcpCredentialFactory;
    private final GenerateSshKey generateSshKey;
    private ProjectCredentials projectCredentials;
    private UserName userName;
    private OsLoginServiceSettings osLoginServiceSettings;
    private GcpSshKey gcpSshKey;

    public GcpOsLoginKeyManager(final GenerateSshKey generateSshKey, final GcpCredentialFactory gcpCredentialFactory) {
        this.generateSshKey = generateSshKey;
        this.gcpCredentialFactory = gcpCredentialFactory;
    }

    @Override
    public GcpKeyManager init() {
        try {
            projectCredentials = gcpCredentialFactory.create();
            userName = UserName.of(projectCredentials.getClientEmail());
            osLoginServiceSettings =
                    OsLoginServiceSettings.newBuilder()
                            .setCredentialsProvider(FixedCredentialsProvider.create(projectCredentials.getCredentials()))
                            .build();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot initialize for " + gcpCredentialFactory.info(), e);
        }
        return this;
    }

    @Override
    public GcpSshKey refreshKey(long expiryInUsec, int keySize) {
        // check if key valid for next second
        if (gcpSshKey == null || System.currentTimeMillis() + 1_000 > this.gcpSshKey.getExpirationTimeMs()) {

            SshKeyPair sshKeyPair = generateSshKey.generate(projectCredentials.getClientEmail(), keySize);
            long expirationTimeUsec = System.currentTimeMillis() * 1000 + expiryInUsec;
            LoginProfile loginProfile = importSssKeyProjectLevel(sshKeyPair, expiryInUsec);
            int posixAccountsCount = loginProfile.getPosixAccountsCount();
            if (posixAccountsCount < 1) {
                throw new IllegalArgumentException("Cannot get account for " + gcpCredentialFactory.info() + " has no posix account");
            }
            OsLoginProto.PosixAccount posixAccount = loginProfile.getPosixAccounts(0);

            Map<String, OsLoginProto.SshPublicKey> sshPublicKeysMap = loginProfile.getSshPublicKeysMap();
            OsLoginProto.SshPublicKey sshPublicKey = sshPublicKeysMap.get(sshKeyPair.getFingerPrint());
            if (sshPublicKey != null) {
                expirationTimeUsec = sshPublicKey.getExpirationTimeUsec();
            }

            logger.debug("Using new key pair for user {} it expires at {} usec", posixAccount.getUsername(), expirationTimeUsec);

            this.gcpSshKey = new GcpSshKey(sshKeyPair, posixAccount.getUsername(), expirationTimeUsec);
        }
        return this.gcpSshKey;
    }

    protected LoginProfile importSssKeyProjectLevel(SshKeyPair sshKeyPair, long expiryInUsec) {
        try (OsLoginServiceClient osLoginServiceClient = OsLoginServiceClient.create(osLoginServiceSettings)) {
            OsLoginProto.SshPublicKey sshPublicKey = createSshPublicKey(sshKeyPair, expiryInUsec);
            ImportSshPublicKeyResponse importSshPublicKeyResponse =
                    osLoginServiceClient.importSshPublicKey(userName, sshPublicKey, projectCredentials.getProjectId());
            return importSshPublicKeyResponse.getLoginProfile();
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot use credentials from " + gcpCredentialFactory.info(), e);
        }
    }

    private OsLoginProto.SshPublicKey createSshPublicKey(SshKeyPair sshKeyPair, long expiryInUsec) {
        return OsLoginProto.SshPublicKey.newBuilder()
                .setKey(sshKeyPair.getPublicKey())
                .setExpirationTimeUsec(System.currentTimeMillis() * 1_000 + expiryInUsec)
                .build();
    }
}
