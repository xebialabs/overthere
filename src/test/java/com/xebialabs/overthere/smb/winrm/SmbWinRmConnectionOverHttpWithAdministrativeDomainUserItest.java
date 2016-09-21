/**
 * Copyright (c) 2008-2016, XebiaLabs B.V., All rights reserved.
 *
 *
 * Overthere is licensed under the terms of the GPLv2
 * <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most XebiaLabs Libraries.
 * There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
 * this software, see the FLOSS License Exception
 * <http://github.com/xebialabs/overthere/blob/master/LICENSE>.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
 * Floor, Boston, MA 02110-1301  USA
 */
package com.xebialabs.overthere.smb.winrm;

import com.google.common.io.Resources;
import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.WindowsCloudHostWithDomainListener;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import com.xebialabs.overthere.smb.SmbProcessConnection;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.WindowsCloudHostWithDomainListener.DOMAIN_WINDOWS_USER_PASSWORD;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_INTERNAL;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.smb.SmbConnectionBuilder.SMB_PROTOCOL;

@Test
@Listeners({WindowsCloudHostWithDomainListener.class})
public class SmbWinRmConnectionOverHttpWithAdministrativeDomainUserItest extends OverthereConnectionItestBase {

    public static final String DOMAIN_WINDOWS_USERNAME = "itest@W2K8R2.XEBIALABS.COM";

    public SmbWinRmConnectionOverHttpWithAdministrativeDomainUserItest() {
        System.setProperty("java.security.krb5.conf", getConfigFilePath());
    }

    @Override
    protected String getProtocol() {
        return SMB_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM_INTERNAL);
        options.set(ADDRESS, WindowsCloudHostWithDomainListener.getHost().getHostName());
        options.set(USERNAME, DOMAIN_WINDOWS_USERNAME);
        options.set(PASSWORD, DOMAIN_WINDOWS_USER_PASSWORD);
        return options;
    }

    private String getConfigFilePath() {
        URL url = Resources.getResource("winrm/conf/krb5.conf");
        File tempFile;
        try {
            tempFile = File.createTempFile("krb5", ".conf");
            Files.copy(url.openStream(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create krb5.conf file");
        }
        tempFile.deleteOnExit();
        return tempFile.getAbsolutePath();
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return SmbProcessConnection.class.getName();
    }
}
