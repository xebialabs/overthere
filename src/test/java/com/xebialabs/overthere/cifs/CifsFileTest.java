package com.xebialabs.overthere.cifs;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection;

import static com.xebialabs.overthere.ConnectionOptions.ADDRESS;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.PASSWORD;
import static com.xebialabs.overthere.ConnectionOptions.PORT;
import static com.xebialabs.overthere.ConnectionOptions.USERNAME;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PORT_DEFAULT;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CIFS_PROTOCOL;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.PORT_DEFAULT_WINRM_HTTP;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.util.DefaultAddressPortMapper.INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class CifsFileTest {

    private ConnectionOptions options;

    @BeforeMethod
    public void setupOptions() {
        options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM_INTERNAL);
        options.set(PASSWORD, "foobar");
        options.set(PORT, PORT_DEFAULT_WINRM_HTTP);
        options.set(CIFS_PORT, CIFS_PORT_DEFAULT);
        options.set(ADDRESS, "localhost");
    }


    @Test
    public void shouldReturnNullForParentFileOfRoot() {
        options.set(USERNAME, "user@domain.com");
        CifsWinRmConnection cifsWinRmConnection = new CifsWinRmConnection(CIFS_PROTOCOL, options, INSTANCE);
        OverthereFile file = cifsWinRmConnection.getFile("C:\\");
        assertThat(file.getParentFile(), nullValue());
    }

    @Test
    public void shouldSucceedForNonRoot() {
        options.set(USERNAME, "user@domain.com");
        CifsWinRmConnection cifsWinRmConnection = new CifsWinRmConnection(CIFS_PROTOCOL, options, INSTANCE);
        OverthereFile file = cifsWinRmConnection.getFile("C:\\windows\\temp\\ot-2015060");
        assertThat(file.getParentFile(), not(nullValue()));
    }


}