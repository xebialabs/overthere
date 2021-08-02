package com.xebialabs.overthere.gcp;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.api.services.compute.ComputeScopes;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Strings;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.Operation;
import com.google.api.services.compute.model.Project;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialFactory;
import com.xebialabs.overthere.gcp.credentials.ProjectCredentials;

/**
 * Key Manager that is provisioning SSH keys on GCP with project or instance metadata. Implementation is based on following documentation
 * <a href="https://cloud.google.com/compute/docs/instances/adding-removing-ssh-keys">Managing SSH keys in metadata</a>.
 */
public class GcpMetadataKeyManager implements GcpKeyManager {
    private static final Logger logger = LoggerFactory.getLogger(GcpMetadataKeyManager.class);

    private static final String SSH_KEYS_KEYNAME = "ssh-keys";
    private static final String SSH_KEYS_USERNAME = "google-ssh";

    private static HttpTransport httpTransport;
    private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

    static {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create new trusted transport", e);
        }
    }

    private final GcpCredentialFactory gcpCredentialFactory;
    private final GenerateSshKey generateSshKey;
    private ProjectCredentials projectCredentials;
    private GcpSshKey gcpSshKey;
    private Compute computeService;
    private static final List<String> SCOPES = Arrays.asList(ComputeScopes.CLOUD_PLATFORM);
    private GoogleCredential credential;

    private final String zoneName;
    private final String instanceId;
    private final String username;
    private final String applicationName;

    GcpMetadataKeyManager(
            final GenerateSshKey generateSshKey,
            final GcpCredentialFactory gcpCredentialFactory,
            final String zoneName,
            final String instanceId,
            final String username,
            final String applicationName) {
        this.generateSshKey = generateSshKey;
        this.gcpCredentialFactory = gcpCredentialFactory;
        this.zoneName = zoneName;
        this.instanceId = instanceId;
        this.username = username;
        this.applicationName = applicationName;
    }

    @Override
    public GcpKeyManager init() {
        System.out.print("CAME 333");
        projectCredentials = gcpCredentialFactory.create();
        System.out.print("CAME 10101010");
        computeService = createComputeService();
        return this;
    }

    @Override
    public GcpSshKey refreshKey(final long expiryInMs, final int keySize) {
        if (gcpSshKey == null || System.currentTimeMillis() + 1_000 > this.gcpSshKey.getExpirationTimeMs()) {

            SshKeyPair sshKeyPair = generateSshKey.generate(SSH_KEYS_USERNAME, keySize);
            long expirationTimeMs = System.currentTimeMillis() + expiryInMs;

            if (this.instanceId == null) {
                addKeyToProject(sshKeyPair.getPublicKey(), expirationTimeMs);
            } else {
                addKeyToInstance(sshKeyPair.getPublicKey(), expirationTimeMs);
            }

            logger.debug("Using new key pair for user {} it expires at {} ms", username, expirationTimeMs);
            this.gcpSshKey = new GcpSshKey(sshKeyPair, username, expirationTimeMs);
        }
        return this.gcpSshKey;
    }

    public String getZoneName() {
        return zoneName;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getUsername() {
        return username;
    }

    public String getApplicationName() {
        return applicationName;
    }

    private void addKeyToInstance(final String publicKey, final long expiryInMs) {
        try {
            Compute.Instances instances = computeService.instances();

            Instance instance = instances.get(projectCredentials.getProjectId(), zoneName, instanceId)
                    .execute();
            Metadata metadata = instance.getMetadata();
            updateSshKey(metadata, publicKey, expiryInMs);
            Operation operation = instances.setMetadata(projectCredentials.getProjectId(), zoneName, instanceId, metadata)
                    .execute();

            checkForOperationErrors(operation);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot install key pairs on project " + projectCredentials.getProjectId() + " and instance " + instanceId + " for username " + username,
                    e
            );
        }
    }

    private void addKeyToProject(final String publicKey, final long expiryInMs) {
        try {
            Compute.Projects projects = computeService.projects();
            Project project = projects.get(projectCredentials.getProjectId())
                    .execute();
            Metadata metadata = project.getCommonInstanceMetadata();
            updateSshKey(metadata, publicKey, expiryInMs);
            Operation operation = projects.setCommonInstanceMetadata(projectCredentials.getProjectId(), metadata)
                    .execute();

            checkForOperationErrors(operation);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot install key pairs on project " + projectCredentials.getProjectId() + " for username " + username,
                    e
            );
        }
    }

    protected void updateSshKey(final Metadata metadata, final String publicKey, final long expiryInMs) {
        List<Metadata.Items> items = metadata.getItems();
        ListIterator<Metadata.Items> itemsListIterator = items.listIterator();
        boolean updated = false;
        while (itemsListIterator.hasNext()) {
            Metadata.Items currentItem = itemsListIterator.next();
            if (SSH_KEYS_KEYNAME.equals(currentItem.getKey())) {
                itemsListIterator.set(composeSshKeyItem(currentItem.getValue(), publicKey, expiryInMs));
                updated = true;
                break;
            }
        }
        if (!updated) {
            itemsListIterator.add(composeSshKeyItem(null, publicKey, expiryInMs));
        }
    }

    protected Metadata.Items composeSshKeyItem(final String sshKeys, final String publicKey, final long expiryInMs) {
        if (Strings.isNullOrEmpty(sshKeys)) {
            return new Metadata.Items()
                    .setKey(SSH_KEYS_KEYNAME)
                    .setValue(composeSshKeyLine(publicKey, expiryInMs));
        }
        String[] sshKeysSplit = sshKeys.split("\n");
        StringBuilder resultSshKeys = new StringBuilder();
        for (String line : sshKeysSplit) {
            if (!isUsernameInLine(line)) {
                resultSshKeys.append(line).append('\n');
            }
        }
        resultSshKeys.append(composeSshKeyLine(publicKey, expiryInMs));
        return new Metadata.Items()
                .setKey(SSH_KEYS_KEYNAME)
                .setValue(resultSshKeys.toString());
    }

    protected boolean isUsernameInLine(final String line) {
        return line != null && line.trim().startsWith(username);
    }

    protected String composeSshKeyLine(String publicKey, final long expiryInMs) {
        return new StringBuilder(username).append(":")
                .append(publicKey.replace('\n', ' '))
                .append(" {\"userName\":\"").append(username)
                .append("\",\"expireOn\":\"").append(getISO8601StringForDate(expiryInMs))
                .append("\"}")
                .toString();
    }

    private void checkForOperationErrors(final Operation operation) {
        if (operation.getError() != null && operation.getError().getErrors() != null && !operation.getError().getErrors().isEmpty()) {
            Operation.Error.Errors errors = operation.getError().getErrors().get(0);
            throw new IllegalStateException(
                    "Cannot install key pairs on project " + projectCredentials.getProjectId() + " for username " + username + ": " + errors.getMessage()
            );
        }
    }

    private Compute createComputeService() {
        System.out.print("CAME 11 11 11 11 11");
        if(projectCredentials.getGoogleCredentials() != null) {
//            GoogleCredential.Builder credentialBuilderNew = new GoogleCredential.Builder().setTransport(httpTransport).setJsonFactory(jsonFactory).setClientSecrets("client_id", "client_secret");
//            this.credential = credentialBuilderNew.build().createScoped(SCOPES);
//            credential.setAccessToken(accessToken);
            System.out.print("CAME 12 12 12 12 12 12 12");
            return new Compute.Builder(
                    httpTransport, jsonFactory, null)
                    .setApplicationName(applicationName)
                    .setHttpRequestInitializer(projectCredentials.getGoogleCredentials())
                    .build();
        } else {
            return new Compute.Builder(
                    httpTransport,
                    jsonFactory,
                    new HttpCredentialsAdapter(projectCredentials.getCredentials())
            ).setApplicationName(applicationName).build();
        }
    }

    private static String getISO8601StringForDate(long date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0000", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }
}
