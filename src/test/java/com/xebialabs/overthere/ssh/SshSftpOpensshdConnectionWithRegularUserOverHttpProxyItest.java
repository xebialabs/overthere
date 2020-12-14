package com.xebialabs.overthere.ssh;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.UnixCloudHostListener;
import com.xebialabs.overthere.WindowsCloudHostListener;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.WindowsCloudHostListener.REGULAR_WINDOWS_USER_PASSWORD;
import static com.xebialabs.overthere.WindowsCloudHostListener.REGULAR_WINDOWS_USER_USERNAME;
import static com.xebialabs.overthere.proxy.ProxyConnection.PROXY_PROTOCOL;
import static com.xebialabs.overthere.proxy.ProxyConnection.PROXY_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.*;
import static com.xebialabs.overthere.ssh.SshConnectionType.SFTP_OpenSSHD;
import static java.net.Proxy.Type.HTTP;

@Test
@Listeners({UnixCloudHostListener.class, WindowsCloudHostListener.class})
public class SshSftpOpensshdConnectionWithRegularUserOverHttpProxyItest extends OverthereConnectionItestBase {

    @Override
    protected String getProtocol() {
        return SSH_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions proxyOptions = new ConnectionOptions();
        proxyOptions.set(PROTOCOL, PROXY_PROTOCOL);
        proxyOptions.set(PROXY_TYPE, HTTP);
        proxyOptions.set(ADDRESS, UnixCloudHostListener.getHost().getHostName());
        proxyOptions.set(PORT, 8888);

        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, SFTP_OpenSSHD);
        options.set(ADDRESS, WindowsCloudHostListener.getHost().getHostName());
        options.set(PORT, 2222);
        options.set(USERNAME, REGULAR_WINDOWS_USER_USERNAME);
        options.set(PASSWORD, REGULAR_WINDOWS_USER_PASSWORD);
        options.set(ALLOCATE_PTY, "xterm:80:24:0:0");
        options.set(JUMPSTATION, proxyOptions);
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return SshSftpOpenSshdConnection.class.getName();
    }

}
