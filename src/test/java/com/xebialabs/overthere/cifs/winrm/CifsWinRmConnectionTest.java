package com.xebialabs.overthere.cifs.winrm;

import com.xebialabs.overthere.ConnectionOptions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM;
import static com.xebialabs.overthere.util.DefaultAddressPortMapper.INSTANCE;

public class CifsWinRmConnectionTest {

    private ConnectionOptions options;

    @BeforeMethod
    public void setupOptions() {
        options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM);
        options.set(PASSWORD, "foobar");
        options.set(PORT, DEFAULT_WINRM_HTTP_PORT);
        options.set(CIFS_PORT, DEFAULT_CIFS_PORT);
        options.set(ADDRESS, "localhost");
    }

    @Test
    @SuppressWarnings("resource")
    public void shouldSupportNewStyleDomainAccount() {
        options.set(USERNAME, "user@domain.com");
        new CifsWinRmConnection(CIFS_PROTOCOL, options, INSTANCE);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("resource")
    public void shouldNotSupportOldStyleDomainAccount() {
        options.set(USERNAME, "domain\\user");
        new CifsWinRmConnection(CIFS_PROTOCOL, options, INSTANCE);
    }

    @Test
    @SuppressWarnings("resource")
    public void shouldSupportDomainlessAccount() {
        options.set(USERNAME, "user");
        new CifsWinRmConnection(CIFS_PROTOCOL, options, INSTANCE);
    }

}
