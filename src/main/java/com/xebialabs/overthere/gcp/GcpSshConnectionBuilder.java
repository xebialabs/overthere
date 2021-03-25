package com.xebialabs.overthere.gcp;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.spi.AddressPortMapper;
import com.xebialabs.overthere.spi.Protocol;
import com.xebialabs.overthere.ssh.GcpSshConnectionConfigurer;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;

import static com.xebialabs.overthere.ConnectionOptions.USERNAME;


@Protocol(name = GcpSshConnectionBuilder.GCP_SSH_PROTOCOL)
public class GcpSshConnectionBuilder extends SshConnectionBuilder {

    /**
     * Name of the protocol handled by this connection builder, i.e. "gcp_ssh".
     */
    public static final String GCP_SSH_PROTOCOL = "gcp_ssh";

    public static final String KEY_MANAGEMENT_TYPE = "ketManagementType";

    public static final String GCP_CREDENTIALS_TYPE = "gcpCredentialsType";

    public static final String CREDENTIALS_FILE = "credentialsFile";

    public static final String CREDENTIALS_JSON = "credentialsJson";

    public static final String TOKEN_VALUE = "tokenValue";

    public static final String TOKEN_EXPIRATION_TIME_MILLIS = "tokenExpirationTimeMillis";

    public static final String PROJECT_ID = "projectId";

    public static final String INSTANCE_ID = "instanceId";

    public static final String CLIENT_ID = "clientId";

    public static final String CLIENT_EMAIL = "clientEmail";

    public static final String PRIVATE_KEY_PKCS8 = "privateKeyPkcs8";

    public static final String PRIVATE_KEY_ID = "privateKeyId";

    public static final String SCOPES = "scopes";

    public static final String TOKEN_SERVER_URI = "tokenServerUri";

    public static final String SERVICE_ACCOUNT_USER = "serviceAccountUser";

    public static final String KEY_SIZE = "keySize";

    public static final String KEY_EXPIRY_TIME_MILLIS = "keyExpiryTimeMillis";

    private final long keyExpiryTimeUsec;

    private final int keySize;

    private GcpKeyManager gcpKeyManager;

    public GcpSshConnectionBuilder(final String type, final ConnectionOptions options, final AddressPortMapper mapper) {
        super(type, additionalConnectionOptions(options), mapper);
        this.gcpKeyManager = GcpKeyManagerFactory.create(options.<String>get(CREDENTIALS_FILE));
        keyExpiryTimeUsec = options.getInteger(KEY_EXPIRY_TIME_MILLIS, 300_000) * 1000L;
        keySize = options.getInteger(KEY_SIZE, 1024);
    }

    @Override
    public OverthereConnection connect() {
        GcpSshKey gcpSshKey = gcpKeyManager.refreshKey(keyExpiryTimeUsec, keySize);
        this.connection = new GcpSshConnectionConfigurer(connection)
                .configureSshConnection(gcpSshKey.getPrivateKey(), gcpSshKey.getUsername());
        return super.connect();
    }

    private static ConnectionOptions additionalConnectionOptions(final ConnectionOptions options) {
        options.set(USERNAME, USERNAME);
        return options;
    }
}
