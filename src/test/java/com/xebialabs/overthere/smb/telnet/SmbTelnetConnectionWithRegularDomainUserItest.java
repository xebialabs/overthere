package com.xebialabs.overthere.smb.telnet;

import com.google.common.collect.ImmutableMap;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.WindowsCloudHostWithDomainListener;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.WindowsCloudHostWithDomainListener.DOMAIN_WINDOWS_USER_PASSWORD;
import static com.xebialabs.overthere.cifs.CifsConnectionType.TELNET;
import static com.xebialabs.overthere.cifs.ConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.ConnectionBuilder.PATH_SHARE_MAPPINGS;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.SMB_PROTOCOL;

@Test
@Listeners({WindowsCloudHostWithDomainListener.class})
public class SmbTelnetConnectionWithRegularDomainUserItest extends OverthereConnectionItestBase {

    public static final String WINDOWS_USERNAME = "W2K8R2\\itest";

    @Override
    protected String getProtocol() {
        return SMB_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, TELNET);
        options.set(ADDRESS, WindowsCloudHostWithDomainListener.getHost().getHostName());
        options.set(USERNAME, WINDOWS_USERNAME);
        options.set(PASSWORD, DOMAIN_WINDOWS_USER_PASSWORD);
        options.set(TEMPORARY_DIRECTORY_PATH, "C:\\overthere\\temp");
        options.set(PATH_SHARE_MAPPINGS, ImmutableMap.of("C:\\overthere", "sharethere"));
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return SmbTelnetConnection.class.getName();
    }

}
