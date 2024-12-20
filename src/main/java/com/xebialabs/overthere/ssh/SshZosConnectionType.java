package com.xebialabs.overthere.ssh;

public enum SshZosConnectionType {
    /**
     * An SSH connection that uses SFTP to transfer files, to a Unix host.
     */
    SFTP,

    /**
     * An SSH connection that uses SCP to transfer files, to a Unix host.
     */
    SCP,
}
