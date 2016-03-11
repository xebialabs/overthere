package com.xebialabs.overthere.proxy;

import com.xebialabs.overthere.*;
import com.xebialabs.overthere.cifs.CifsConnectionBuilder;
import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;


import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.proxy.ProxyConnection.PROXY_TYPE;
import static com.xebialabs.overthere.ssh.SshConnectionBuilder.SSH_PROTOCOL;
import static com.xebialabs.overthere.ssh.SshConnectionType.SCP;
import static java.net.Proxy.Type.HTTP;
import static java.net.Proxy.Type.SOCKS;
import static org.testng.Assert.fail;

@Test
@Listeners({UnixCloudHostListener.class, WindowsCloudHostListener.class})
public class ProxyTypeItest {

    @Test
    public void shouldNotConnectOverSocksProxy() {
        try {
            ConnectionOptions proxyOptions = new ConnectionOptions();
            proxyOptions.set(PROTOCOL, "proxy");
            proxyOptions.set(PROXY_TYPE, SOCKS);
            proxyOptions.set(ADDRESS, UnixCloudHostListener.getHost().getHostName());
            proxyOptions.set(PORT, 1080);

            ConnectionOptions options = new ConnectionOptions();
            options.set(SshConnectionBuilder.CONNECTION_TYPE, SCP);
            options.set(OPERATING_SYSTEM, WINDOWS);
            options.set(ADDRESS, WindowsCloudHostListener.getHost().getHostName());
            options.set(USERNAME, "username");
            options.set(PASSWORD, "password");
            options.set(JUMPSTATION, proxyOptions);

            Overthere.getConnection(SSH_PROTOCOL, options);

            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void shouldNotConnectCifsOverProxy() {
        try {
            ConnectionOptions proxyOptions = new ConnectionOptions();
            proxyOptions.set(PROTOCOL, "proxy");
            proxyOptions.set(PROXY_TYPE, HTTP);
            proxyOptions.set(ADDRESS, UnixCloudHostListener.getHost().getHostName());
            proxyOptions.set(PORT, 8888);

            ConnectionOptions options = new ConnectionOptions();
            options.set(CifsConnectionBuilder.CONNECTION_TYPE, WINRM_INTERNAL);
            options.set(OPERATING_SYSTEM, WINDOWS);
            options.set(ADDRESS, WindowsCloudHostListener.getHost().getHostName());
            options.set(USERNAME, "username");
            options.set(PASSWORD, "password");
            options.set(JUMPSTATION, proxyOptions);

            Overthere.getConnection(CIFS_PROTOCOL, options);

            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

}
