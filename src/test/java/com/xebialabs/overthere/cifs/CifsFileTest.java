package com.xebialabs.overthere.cifs;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.cifs.winrm.CifsWinRmConnection;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.CifsConnectionBuilder.*;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.util.DefaultAddressPortMapper.INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.testng.Assert.*;

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


}