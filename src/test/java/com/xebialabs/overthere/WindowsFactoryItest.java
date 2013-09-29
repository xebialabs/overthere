package com.xebialabs.overthere;

import static com.google.common.collect.Lists.newArrayList;
import static com.xebialabs.overthere.ConnectionOptions.JUMPSTATION;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_TELNET_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTPS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTP_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PATH_SHARE_MAPPINGS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_ENABLE_HTTPS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_PTY;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP_CYGWIN;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP_WINSSHD;

import java.util.List;

import org.testng.annotations.Factory;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy;
import com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionType;

public class WindowsFactoryItest {
    private static final String ADMINISTRATIVE_USER_ITEST_USERNAME = "Administrator";
    private static final String ADMINISTRATIVE_USER_ITEST_PASSWORD = "iW8tcaM0d";

    private static final String REGULAR_USER_ITEST_USERNAME = "overthere";
    // The password for the regular user includes special characters to test that they get encoded correctly
    private static final String REGULAR_USER_ITEST_PASSWORD = "wLitdMy@:;<>KY9";

    @Factory
    public Object[] createWindowsTests() throws Exception {
        List<Object> itests = newArrayList();
        itests.add(cifsTelnetWithAdministrativeUser());
        itests.add(cifsTelnetWithRegularUser());
        itests.add(cifsWinRmHttpWithAdministrativeUser());
        itests.add(cifsWinRmHttpsWithAdministrativeUser());
        itests.add(sshSftpCygwinWithAdministrativeUser());
        itests.add(sshSftpCygwinWithRegularUser());
        itests.add(sshSftpWinSshdWithAdministrativeUser());
        itests.add(sshSftpWinSshdWithRegularUser());
        return itests.toArray(new Object[itests.size()]);
    }

    private OverthereConnectionItest cifsTelnetWithAdministrativeUser() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, TELNET);
        options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
        options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
        options.set(PORT, DEFAULT_TELNET_PORT);
        options.set(CIFS_PORT, DEFAULT_CIFS_PORT);
        options.set(JUMPSTATION, createPartialTunnelOptions());
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_cifsTelnetWithAdministrativeUser", CIFS_PROTOCOL, options,
            "com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection", "overthere-windows");
    }

    private OverthereConnectionItest cifsTelnetWithRegularUser() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, TELNET);
        options.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
        options.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
        options.set(PORT, DEFAULT_TELNET_PORT);
        options.set(CIFS_PORT, DEFAULT_CIFS_PORT);
        options.set(TEMPORARY_DIRECTORY_PATH, "C:\\overthere\\tmp");
        options.set(PATH_SHARE_MAPPINGS, ImmutableMap.of("C:\\overthere", "sharethere"));
        options.set(JUMPSTATION, createPartialTunnelOptions());
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_cifsTelnetWithRegularUser", CIFS_PROTOCOL, options,
            "com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection", "overthere-windows");
    }

    private OverthereConnectionItest cifsWinRmHttpWithAdministrativeUser() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM_INTERNAL);
        options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
        options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
        options.set(WINRM_CONTEXT, DEFAULT_WINRM_CONTEXT);
        options.set(PORT, DEFAULT_WINRM_HTTP_PORT);
        options.set(CIFS_PORT, DEFAULT_CIFS_PORT);
        options.set(JUMPSTATION, createPartialTunnelOptions());
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_cifsWinRmHttpWithAdministrativeUser", CIFS_PROTOCOL, options,
            "com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection", "overthere-windows");
    }

    private OverthereConnectionItest cifsWinRmHttpsWithAdministrativeUser() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM_INTERNAL);
        options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
        options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
        options.set(WINRM_CONTEXT, DEFAULT_WINRM_CONTEXT);
        options.set(PORT, DEFAULT_WINRM_HTTPS_PORT);
        options.set(CIFS_PORT, DEFAULT_CIFS_PORT);
        options.set(WINRM_ENABLE_HTTPS, true);
        options.set(WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY, WinrmHttpsCertificateTrustStrategy.ALLOW_ALL);
        options.set(WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY, WinrmHttpsHostnameVerificationStrategy.ALLOW_ALL);
        options.set(JUMPSTATION, createPartialTunnelOptions());
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_cifsWinRmHttpsWithAdministrativeUser", CIFS_PROTOCOL, options,
            "com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection", "overthere-windows");
    }

    private OverthereConnectionItest sshSftpCygwinWithAdministrativeUser() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, SFTP_CYGWIN);
        options.set(PORT, 22);
        options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
        options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_sshSftpCygwinWithAdministrativeUser", SSH_PROTOCOL, options,
            "com.xebialabs.overthere.ssh.SshSftpCygwinConnection", "overthere-windows");
    }

    private OverthereConnectionItest sshSftpCygwinWithRegularUser() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, SFTP_CYGWIN);
        options.set(PORT, 22);
        options.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
        options.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_sshSftpCygwinWithRegularUser", SSH_PROTOCOL, options,
            "com.xebialabs.overthere.ssh.SshSftpCygwinConnection", "overthere-windows");
    }

    private OverthereConnectionItest sshSftpWinSshdWithAdministrativeUser() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, SFTP_WINSSHD);
        options.set(PORT, 2222);
        options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
        options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_sshSftpWinSshdWithAdministrativeUser", SSH_PROTOCOL, options,
            "com.xebialabs.overthere.ssh.SshSftpWinSshdConnection", "overthere-windows");
    }

    private OverthereConnectionItest sshSftpWinSshdWithRegularUser() throws Exception {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, SFTP_WINSSHD);
        options.set(PORT, 2222);
        options.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
        options.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
        options.set(ALLOCATE_PTY, "xterm:80:24:0:0");
        return new OverthereConnectionItest(this.getClass().getSimpleName() + "_sshSftpWinSshdWithRegularUser", SSH_PROTOCOL, options,
            "com.xebialabs.overthere.ssh.SshSftpWinSshdConnection", "overthere-windows");
    }

    private static ConnectionOptions createPartialTunnelOptions() {
        ConnectionOptions tunnelOptions = new ConnectionOptions();
        tunnelOptions.set(OPERATING_SYSTEM, WINDOWS);
        tunnelOptions.set(SshConnectionBuilder.CONNECTION_TYPE, SshConnectionType.TUNNEL);
        tunnelOptions.set(PORT, 22);
        tunnelOptions.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
        tunnelOptions.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
        return tunnelOptions;
    }

}
