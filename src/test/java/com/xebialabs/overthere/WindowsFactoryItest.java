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
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_TELNET_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_CONTEXT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTPS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.DEFAULT_WINRM_HTTP_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PATH_SHARE_MAPPINGS;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTP;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_HTTPS;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.ALLOCATE_PTY;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP_CYGWIN;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP_WINSSHD;

import java.util.List;

import nl.javadude.assumeng.AssumptionListener;

import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.overthere.cifs.WinrmHttpsCertificateTrustStrategy;
import com.xebialabs.overthere.cifs.WinrmHttpsHostnameVerificationStrategy;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionType;

@Listeners(AssumptionListener.class)
public class WindowsFactoryItest {
	private static final String ADMINISTRATIVE_USER_ITEST_USERNAME = "Administrator";
	private static final String ADMINISTRATIVE_USER_ITEST_PASSWORD = "iW8tcaM0d";

	private static final String REGULAR_USER_ITEST_USERNAME = "overthere";
	// The password for the regular user includes special characters to test that they get encoded correctly
	private static final String REGULAR_USER_ITEST_PASSWORD = "wLitdMy@:;<>KY9";

	@Factory
	public Object[] createWindowsTests() throws Exception {
        List<Object> itests = newArrayList();
		itests.add(new OverthereConnectionItest(CIFS_PROTOCOL, createCifsTelnetWithAdministrativeUserOptions(), "com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection", "overthere-windows"));
		itests.add(new OverthereConnectionItest(CIFS_PROTOCOL, createCifsTelnetWithRegularUserOptions(), "com.xebialabs.overthere.cifs.telnet.CifsTelnetConnection", "overthere-windows"));
		itests.add(new OverthereConnectionItest(CIFS_PROTOCOL, createCifsWinRmHttpWithAdministrativeUserOptions(), "com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection", "overthere-windows"));
		itests.add(new OverthereConnectionItest(CIFS_PROTOCOL, createCifsWinRmHttpsWithAdministrativeUserOptions(), "com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection", "overthere-windows"));
		itests.add(new OverthereConnectionItest(SSH_PROTOCOL, createSshSftpCygwinWithAdministrativeUserOptions(), "com.xebialabs.overthere.ssh.SshSftpCygwinConnection", "overthere-windows"));
		itests.add(new OverthereConnectionItest(SSH_PROTOCOL, createSshSftpCygwinWithRegularUserOptions(), "com.xebialabs.overthere.ssh.SshSftpCygwinConnection", "overthere-windows"));
		itests.add(new OverthereConnectionItest(SSH_PROTOCOL, createSshSftpWinSshdWithAdministrativeUserOptions(), "com.xebialabs.overthere.ssh.SshSftpWinSshdConnection", "overthere-windows"));
		itests.add(new OverthereConnectionItest(SSH_PROTOCOL, createSshSftpWinSshdWithRegularUserOptions(), "com.xebialabs.overthere.ssh.SshSftpWinSshdConnection", "overthere-windows"));
		return itests.toArray(new Object[itests.size()]);
	}

	private static ConnectionOptions createCifsTelnetWithAdministrativeUserOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, TELNET);
		partialOptions.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		partialOptions.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		partialOptions.set(PORT, DEFAULT_TELNET_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
		partialOptions.set(JUMPSTATION, createPartialTunnelOptions());
		return partialOptions;
	}

	private static ConnectionOptions createCifsTelnetWithRegularUserOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, TELNET);
		partialOptions.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
		partialOptions.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
		partialOptions.set(PORT, DEFAULT_TELNET_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
		partialOptions.set(TEMPORARY_DIRECTORY_PATH, "C:\\overthere\\tmp");
		partialOptions.set(PATH_SHARE_MAPPINGS, ImmutableMap.of("C:\\overthere", "sharethere"));
		partialOptions.set(JUMPSTATION, createPartialTunnelOptions());
		return partialOptions;
	}

	private static ConnectionOptions createCifsWinRmHttpWithAdministrativeUserOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, WINRM_HTTP);
		partialOptions.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		partialOptions.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PORT, DEFAULT_WINRM_HTTP_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
		partialOptions.set(JUMPSTATION, createPartialTunnelOptions());
		return partialOptions;
	}

	private static ConnectionOptions createCifsWinRmHttpsWithAdministrativeUserOptions() {
		ConnectionOptions partialOptions = new ConnectionOptions();
		partialOptions.set(OPERATING_SYSTEM, WINDOWS);
		partialOptions.set(CONNECTION_TYPE, WINRM_HTTPS);
		partialOptions.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		partialOptions.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		partialOptions.set(CONTEXT, DEFAULT_WINRM_CONTEXT);
		partialOptions.set(PORT, DEFAULT_WINRM_HTTPS_PORT);
		partialOptions.set(CIFS_PORT, DEFAULT_CIFS_PORT);
		partialOptions.set(WINRM_HTTPS_CERTIFICATE_TRUST_STRATEGY, WinrmHttpsCertificateTrustStrategy.ALLOW_ALL);
		partialOptions.set(WINRM_HTTPS_HOSTNAME_VERIFICATION_STRATEGY, WinrmHttpsHostnameVerificationStrategy.ALLOW_ALL);
		partialOptions.set(JUMPSTATION, createPartialTunnelOptions());
		return partialOptions;
	}

	private static ConnectionOptions createSshSftpCygwinWithAdministrativeUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_CYGWIN);
		options.set(PORT, 22);
		options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		options.set(JUMPSTATION, createPartialTunnelOptions());
		return options;
	}

	private static ConnectionOptions createSshSftpCygwinWithRegularUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_CYGWIN);
		options.set(PORT, 22);
		options.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
		options.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
		options.set(JUMPSTATION, createPartialTunnelOptions());
		return options;
	}

	private static ConnectionOptions createSshSftpWinSshdWithAdministrativeUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_WINSSHD);
		options.set(PORT, 2222);
		options.set(USERNAME, ADMINISTRATIVE_USER_ITEST_USERNAME);
		options.set(PASSWORD, ADMINISTRATIVE_USER_ITEST_PASSWORD);
		options.set(JUMPSTATION, createPartialTunnelOptions());
		return options;
	}

	private static ConnectionOptions createSshSftpWinSshdWithRegularUserOptions() {
		ConnectionOptions options = new ConnectionOptions();
		options.set(OPERATING_SYSTEM, WINDOWS);
		options.set(CONNECTION_TYPE, SFTP_WINSSHD);
		options.set(PORT, 2222);
		options.set(USERNAME, REGULAR_USER_ITEST_USERNAME);
		options.set(PASSWORD, REGULAR_USER_ITEST_PASSWORD);
		options.set(ALLOCATE_PTY, "xterm:80:24:0:0");
		options.set(JUMPSTATION, createPartialTunnelOptions());
		return options;
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
