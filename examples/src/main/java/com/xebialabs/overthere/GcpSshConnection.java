package com.xebialabs.overthere;

import com.xebialabs.overthere.gcp.GcpSshConnectionBuilder;
import com.xebialabs.overthere.util.DefaultAddressPortMapper;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.gcp.GcpSshConnectionBuilder.CREDENTIALS_FILE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;

/**
 * Use GCP's os-login for service-account SSH connection.
 * Prepare account according to the following setup: <a href="https://cloud.google.com/compute/docs/instances/managing-instance-access">Setting up OS Login</a>
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
 *  # or add osAdminLogin or osLogin on instance level
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
 * java com.xebialabs.overthere.GcpSshConnection external_ip_address path_to_credentials_json
 * </pre>
 */
public class GcpSshConnection {

    public static void main(String[] args) {

        assert args.length == 2;

        ConnectionOptions connectionOptions = new ConnectionOptions();
        connectionOptions.set(CONNECTION_TYPE, SCP);
        connectionOptions.set(OPERATING_SYSTEM, OperatingSystemFamily.UNIX);
        connectionOptions.set(ADDRESS, args[0]);
        connectionOptions.set(CREDENTIALS_FILE, args[1]);
        OverthereConnection connection = new GcpSshConnectionBuilder(
                GcpSshConnectionBuilder.GCP_SSH_PROTOCOL, connectionOptions, DefaultAddressPortMapper.INSTANCE)
                .connect();

        try {
            assert connection.execute(CmdLine.build("cat", "/etc/motd")) == 0;
            OverthereFile motd = connection.getFile("/etc/motd");
            System.out.println("Length        : " + motd.length());
            System.out.println("Exists        : " + motd.exists());
            System.out.println("Can read      : " + motd.canRead());
            System.out.println("Can write     : " + motd.canWrite());
            System.out.println("Can execute   : " + motd.canExecute());
        } finally {
            connection.close();
        }
    }
}
