package com.xebialabs.overthere;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.xebialabs.overthere.gcp.GcpKeyManagementType;
import com.xebialabs.overthere.gcp.GcpSshConnectionBuilder;
import com.xebialabs.overthere.gcp.credentials.GcpCredentialsType;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_CREDENTIALS_TYPE;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.GCP_KEY_MANAGEMENT_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;

/**
 * Uses GCP's os-login for service-account SSH connection.
 * Prepare account according to the following setup:
 * <a href="https://cloud.google.com/compute/docs/instances/managing-instance-access">Setting up OS Login</a>.
 *
 * <pre>
 *  export PROJECT_ID='my_project'
 *  export ZONE_ID='europe-west1-b'
 *  export SERVICE_ACCOUNT='ssh-account'
 *  export NETWORK_NAME='ssh-example'
 *  export TARGET_INSTANCE_NAME='target'
 *
 *  # create service account
 *  gcloud iam service-accounts create $SERVICE_ACCOUNT --project $PROJECT_ID \
 *  --display-name "$SERVICE_ACCOUNT"
 *
 *  # create network
 *  gcloud compute networks create $NETWORK_NAME --project $PROJECT_ID
 *  gcloud compute firewall-rules create ssh-all --project $PROJECT_ID \
 *  --network $NETWORK_NAME --allow tcp:22
 *
 *  # create target compute instance
 *  gcloud compute instances create $TARGET_INSTANCE_NAME --project $PROJECT_ID \
 *  --zone $ZONE_ID --network $NETWORK_NAME \
 *  --no-service-account --no-scopes \
 *  --machine-type e2-micro --metadata=enable-oslogin=TRUE \
 *  --no-restart-on-failure --maintenance-policy=TERMINATE --preemptible
 *
 *  # add osAdminLogin or osLogin on instance level
 *  gcloud compute instances add-iam-policy-binding $TARGET_INSTANCE_NAME \
 *  --project $PROJECT_ID --zone $ZONE_ID \
 *  --member serviceAccount:$SERVICE_ACCOUNT@$PROJECT_ID.iam.gserviceaccount.com \
 *  --role roles/compute.osAdminLogin
 *
 *  # or add osAdminLogin or osLogin on project level
 *  gcloud projects add-iam-policy-binding $PROJECT_ID \
 *  --member serviceAccount:$SERVICE_ACCOUNT@$PROJECT_ID.iam.gserviceaccount.com \
 *  --role roles/compute.osAdminLogin
 *
 *  # get external IP
 *  gcloud compute instances describe $TARGET_INSTANCE_NAME \
 *  --project $PROJECT_ID --zone $ZONE_ID
 *
 *  # create service account credentials JSON
 *  gcloud iam service-accounts keys create path_to_credentials_json \
 *  --iam-account $SERVICE_ACCOUNT@$PROJECT_ID.iam.gserviceaccount.com
 * </pre>
 *
 * Connect to GCP with example:
 * <pre>
 * java com.xebialabs.overthere.GcpOsLoginSshConnection external_ip_address path_to_credentials_json
 * </pre>
 */
public class GcpOsLoginSshConnection {

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            throw new IllegalArgumentException("Use input arguments: external_ip_address path_to_credentials_json");
        }

        ConnectionOptions connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SCP);
        connectionOptions.set(GCP_CREDENTIALS_TYPE, GcpCredentialsType.ServiceAccountJsonFile.name());
        connectionOptions.set(CREDENTIALS_FILE, args[1]);
        connectionOptions.set(GCP_KEY_MANAGEMENT_TYPE, GcpKeyManagementType.OsLogin.name());
        connectionOptions.set(OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
        connectionOptions.set(ADDRESS, args[0]);
        OverthereConnection connection = new GcpSshConnectionBuilder(
                GcpSshConnectionBuilder.GCP_SSH_PROTOCOL, connectionOptions, DefaultAddressPortMapper.INSTANCE)
                .connect();

        try {
            OverthereProcess process = connection.startProcess(CmdLine.build("cat", "/etc/motd"));
            BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getStdout()));
            try {
                String line;
                while((line = stdout.readLine()) != null) {
                    System.err.println(line);
                }
            } finally {
                stdout.close();
            }
            int exitCode = process.waitFor();
            System.err.println("Exit code from process: " + exitCode);

            OverthereFile motd = connection.getFile("/etc/motd");
            System.err.println("Length        : " + motd.length());
            System.err.println("Exists        : " + motd.exists());
            System.err.println("Can read      : " + motd.canRead());
            System.err.println("Can write     : " + motd.canWrite());
            System.err.println("Can execute   : " + motd.canExecute());
        } finally {
            connection.close();
        }
    }
}
