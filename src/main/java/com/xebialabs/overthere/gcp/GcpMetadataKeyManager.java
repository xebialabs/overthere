package com.xebialabs.overthere.gcp;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Strings;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.model.Instance;
import com.google.api.services.compute.model.Metadata;
import com.google.api.services.compute.model.Operation;
import com.google.api.services.compute.model.Project;
import com.google.auth.Credentials;
import com.google.auth.http.HttpCredentialsAdapter;

import com.xebialabs.overthere.gcp.credentials.GcpCredentialFactory;

/**
 * It is requering additional IAM permissions `compute.instances.setMetadata`
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
    private Credentials credentials;
    private GcpSshKey gcpSshKey;
    private Compute computeService;

    private String projectId;
    private String zoneName;
    private String instanceName;
    private String username;

    public GcpMetadataKeyManager(
            final GenerateSshKey generateSshKey,
            final GcpCredentialFactory gcpCredentialFactory,
            final String projectId,
            final String zoneName,
            final String instanceName,
            final String username) {
        this.generateSshKey = generateSshKey;
        this.gcpCredentialFactory = gcpCredentialFactory;
        this.projectId = projectId;
        this.zoneName = zoneName;
        this.instanceName = instanceName;
        this.username = username;
    }

    @Override
    public GcpKeyManager init() {
        credentials = gcpCredentialFactory.create();
        computeService = createComputeService();

        return this;
    }

    @Override
    public GcpSshKey refreshKey(final long expiryInUsec, final int keySize) {
        if (gcpSshKey == null || System.currentTimeMillis() + 1_000 > this.gcpSshKey.getExpirationTimeMs()) {

            SshKeyPair sshKeyPair = generateSshKey.generate(SSH_KEYS_USERNAME, keySize);
            long expirationTimeMs = System.currentTimeMillis() + expiryInUsec / 1_000;

            if (this.instanceName == null) {
                addKeyToProject(expirationTimeMs);
            } else {
                addKeyToInstance(expirationTimeMs);
            }

            logger.debug("Using new key pair for user {} it expires at {} ms", username, expirationTimeMs);
            this.gcpSshKey = new GcpSshKey(sshKeyPair, username, expirationTimeMs * 1000);
        }
        return this.gcpSshKey;
    }

    private void addKeyToInstance(final long expiryInMs) {
        try {
            Compute.Instances instances = computeService.instances();

            Instance instance = instances.get(projectId, zoneName, instanceName)
                    .execute();
            Metadata metadata = instance.getMetadata();
            updateSshKey(metadata, , expiryInMs)
            Operation operation = instances.setMetadata(projectId, zoneName, instanceName, metadata).execute();

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot install key pairs on project " + projectId + " and instance " + instanceName + " for username " + username, e);
        }
    }

    private void addKeyToProject(final long expiryInMs) {
        try {
            Compute.Projects projects = computeService.projects();
            Project project = projects.get(projectId)
                    .execute();
            Metadata metadata = project.getCommonInstanceMetadata();
            updateSshKey(metadata, , expiryInMs)
            Operation operation = projects.setCommonInstanceMetadata(projectId, metadata)
                    .execute();

            checkForOperationErrors(operation);

        } catch (IOException e) {
            throw new IllegalStateException("Cannot install key pairs on project " + projectId + " for username " + username, e);
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
            return new Metadata.Items().set(SSH_KEYS_KEYNAME, composeSshKeyLine(publicKey, expiryInMs));
        }
        String[] sshKeysSplit = sshKeys.split("\n");
        StringBuilder resultSshKeys = new StringBuilder();
        for (String line : sshKeysSplit) {
            if (!isUsernameInLine(line)) {
                resultSshKeys.append(line).append('\n');
            }
        }
        resultSshKeys.append(composeSshKeyLine(publicKey, expiryInMs));
        return new Metadata.Items().set(SSH_KEYS_KEYNAME, resultSshKeys.toString());
    }

    protected boolean isUsernameInLine(final String line) {
        return line != null && line.trim().startsWith(username);
    }

    protected String composeSshKeyLine(String publicKey, final long expiryInMs) {
        return new StringBuilder(username).append(":")
                .append(publicKey)
                .append(" {\"userName\":\"").append(username)
                .append("\",\"expireOn\":\"").append(new DateTime(expiryInMs).toStringRfc3339())
                .append("\"}")
                .toString();
    }

    private Compute createComputeService() {
        return new Compute.Builder(httpTransport, jsonFactory, new HttpCredentialsAdapter(credentials))
                .build();
    }

    private void checkForOperationErrors(final Operation operation) {
        if (operation.getError() != null && operation.getError().getErrors() != null && !operation.getError().getErrors().isEmpty()) {
            Operation.Error.Errors errors = operation.getError().getErrors().get(0);
            throw new IllegalStateException("Cannot install key pairs on project " + projectId + " for username " + username + ": " +
                    errors.getMessage());
        }
    }
}
