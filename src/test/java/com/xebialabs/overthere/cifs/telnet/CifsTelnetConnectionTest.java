package com.xebialabs.overthere.cifs.telnet;

import com.xebialabs.overthere.ConnectionOptions;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.util.DefaultAddressPortMapper.INSTANCE;

public class CifsTelnetConnectionTest {

    private ConnectionOptions options;

    @BeforeMethod
    public void setupOptions() {
        options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, TELNET);
        options.set(PASSWORD, "foobar");
        options.set(PORT, DEFAULT_TELNET_PORT);
        options.set(CIFS_PORT, DEFAULT_CIFS_PORT);
        options.set(ADDRESS, "localhost");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    @SuppressWarnings("resource")
    public void shouldNotSupportNewStyleDomainAccount() {
        options.set(USERNAME, "user@domain.com");
        new CifsTelnetConnection(CIFS_PROTOCOL, options, INSTANCE);
    }

    @Test
    @SuppressWarnings("resource")
    public void shouldSupportOldStyleDomainAccount() {
        options.set(USERNAME, "domain\\user");
        new CifsTelnetConnection(CIFS_PROTOCOL, options, INSTANCE);
    }

    @Test
    @SuppressWarnings("resource")
    public void shouldSupportDomainlessAccount() {
        options.set(USERNAME, "user");
        new CifsTelnetConnection(CIFS_PROTOCOL, options, INSTANCE);
    }
}
