package com.xebialabs.overthere.gcp;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.RuntimeIOException;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.Protocol;
import com.xebialabs.overthere.ssh.GcpSshConnectionConfigurer;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import net.schmizz.sshj.userauth.UserAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.xebialabs.overthere.ConnectionOptions.USERNAME;


@Protocol(name = GcpSshConnectionBuilder.GCP_SSH_PROTOCOL)
public class GcpSshConnectionBuilder extends SshConnectionBuilder {
    private static final Logger logger = LoggerFactory.getLogger(GcpSshConnectionBuilder.class);

    /**
     * Name of the protocol handled by this connection builder, i.e. "gcp_ssh".
     */
    public static final String GCP_SSH_PROTOCOL = "gcp_ssh";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_gcpKeyManagementType">the online documentation</a>
     */
    public static final String GCP_KEY_MANAGEMENT_TYPE = "gcpKeyManagementType";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_gcpCredentialsType">the online documentation</a>
     */
    public static final String GCP_CREDENTIALS_TYPE = "gcpCredentialsType";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_credentialsFile">the online documentation</a>
     */
    public static final String CREDENTIALS_FILE = "credentialsFile";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_credentialsJson">the online documentation</a>
     */
    public static final String CREDENTIALS_JSON = "credentialsJson";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_projectId">the online documentation</a>
     */
    public static final String PROJECT_ID = "projectId";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_zoneName">the online documentation</a>
     */
    public static final String ZONE_NAME = "zoneName";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_instanceId">the online documentation</a>
     */
    public static final String INSTANCE_ID = "instanceId";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_applicationName">the online documentation</a>
     */
    public static final String APPLICATION_NAME = "applicationName";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_clientId">the online documentation</a>
     */
    public static final String CLIENT_ID = "clientId";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_clientEmail">the online documentation</a>
     */
    public static final String CLIENT_EMAIL = "clientEmail";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_privateKeyPkcs8">the online documentation</a>
     */
    public static final String PRIVATE_KEY_PKCS8 = "privateKeyPkcs8";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_privateKeyId">the online documentation</a>
     */
    public static final String PRIVATE_KEY_ID = "privateKeyId";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_scopes">the online documentation</a>
     */
    public static final String SCOPES = "scopes";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_tokenServerUri">the online documentation</a>
     */
    public static final String TOKEN_SERVER_URI = "tokenServerUri";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_serviceAccountUser">the online documentation</a>
     */
    public static final String SERVICE_ACCOUNT_USER = "serviceAccountUser";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_keySize">the online documentation</a>
     */
    public static final String KEY_SIZE = "keySize";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_keyExpiryTimeMillis">the online documentation</a>
     */
    public static final String KEY_EXPIRY_TIME_MILLIS = "keyExpiryTimeMillis";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_retryCountd">the online documentation</a>
     */
    public static final String RETRY_COUNT = "retryCount";

    /**
     * See <a href="https://github.com/xebialabs/overthere/blob/master/README.md#gcp_retryPeriodMillis">the online documentation</a>
     */
    public static final String RETRY_PERIOD_MILLIS = "retryPeriodMillis";

    private final long keyExpiryTimeMillis;

    private final int keySize;

    private final int retryCount;

    private final int retryPeriodMillis;

    private GcpKeyManager gcpKeyManager;

    public GcpSshConnectionBuilder(final String type, final ConnectionOptions options, final AddressPortMapper mapper) {
        super(type, additionalConnectionOptions(options), mapper);
        this.gcpKeyManager = GcpKeyManagerFactory.create(options);
        keyExpiryTimeMillis = options.getInteger(KEY_EXPIRY_TIME_MILLIS, 300_000);
        keySize = options.getInteger(KEY_SIZE, 1024);
        retryCount = options.getInteger(RETRY_COUNT, 10);
        retryPeriodMillis = options.getInteger(RETRY_PERIOD_MILLIS, 1000);
    }

    @Override
    public OverthereConnection connect() {
        GcpSshKey gcpSshKey = gcpKeyManager.refreshKey(keyExpiryTimeMillis, keySize);
        this.connection = new GcpSshConnectionConfigurer(connection)
                .configureSshConnection(gcpSshKey.getPrivateKey(), gcpSshKey.getUsername());

        int currentRetryCount = 1;
        while (currentRetryCount < retryCount) { // retry connect, the GCP needs time to provision public key on platform
            try {
                return tryToConnect();
            } catch (RuntimeIOException e) {
                if (e.getCause() instanceof UserAuthException) {
                    try {
                        Thread.sleep(retryPeriodMillis);
                    } catch (InterruptedException interruptedException) {
                        throw e;
                    }
                    currentRetryCount++;
                    logger.debug("Connection retry #{} for username {}", currentRetryCount, gcpSshKey.getUsername());
                } else {
                    throw e;
                }
            }
        }
        return tryToConnect();
    }

    void setGcpKeyManager(final GcpKeyManager gcpKeyManager) {
        this.gcpKeyManager = gcpKeyManager;
    }

    public long getKeyExpiryTimeMillis() {
        return keyExpiryTimeMillis;
    }

    public int getKeySize() {
        return keySize;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getRetryPeriodMillis() {
        return retryPeriodMillis;
    }

    protected OverthereConnection tryToConnect() {
        return super.connect();
    }

    private static ConnectionOptions additionalConnectionOptions(final ConnectionOptions options) {
        if (!options.containsKey(USERNAME)) {
            options.set(USERNAME, USERNAME); // temporary dummy username, real is set in the GcpSshConnectionConfigurer
        }
        return options;
    }
}
