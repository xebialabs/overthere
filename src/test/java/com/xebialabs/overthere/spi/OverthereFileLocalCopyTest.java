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
package com.xebialabs.overthere.spi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.common.io.OutputSupplier;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.Overthere;
import com.xebialabs.overthere.OverthereConnection;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.TemporaryFolder;

import static com.google.common.io.ByteStreams.write;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.OperatingSystemFamily.getLocalHostOperatingSystemFamily;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OverthereFileLocalCopyTest {

    private ConnectionOptions options;
    private String protocol;
    private OverthereConnection connection;

    public TemporaryFolder temp = new TemporaryFolder();
    private OverthereConnection otherConnection;
    private ConnectionOptions otherOptions;

    @BeforeMethod
    public void setTypeAndOptions() throws IOException {
        temp.create();
        protocol = LOCAL_PROTOCOL;
        options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, getLocalHostOperatingSystemFamily());
        options.set(TEMPORARY_DIRECTORY_PATH, temp.getRoot().getPath());
        connection = Overthere.getConnection(protocol, options);
        otherOptions = new ConnectionOptions();
        otherOptions.set(OPERATING_SYSTEM, getLocalHostOperatingSystemFamily());
        otherOptions.set(TEMPORARY_DIRECTORY_PATH, temp.newFolder("temp").getPath());
        otherConnection = Overthere.getConnection(protocol, otherOptions);
    }

    @AfterMethod
    public void cleanup() {
        temp.delete();
    }

    @Test
    public void shouldDoLocalCopyIfOverSameConnection() throws IOException {
        final OverthereFile tempFile = connection.getTempFile("Foo.txt");
        write(generateRandomBytes(1000), new OutputSupplier<OutputStream>() {
            @Override
            public OutputStream getOutput() throws IOException {
                return tempFile.getOutputStream();
            }
        });
        BaseOverthereFile spy = mock(BaseOverthereFile.class);
        when(spy.getConnection()).thenReturn((BaseOverthereConnection) connection);
        tempFile.copyTo(spy);
        verify(spy, times(1)).shortCircuitCopyFrom(tempFile);
    }

    @Test
    public void shouldNotDoLocalCopyIfDifferentConnection() throws IOException {
        final OverthereFile tempFile = connection.getTempFile("Foo.txt");
        write(generateRandomBytes(1000), new OutputSupplier<OutputStream>() {
            @Override
            public OutputStream getOutput() throws IOException {
                return tempFile.getOutputStream();
            }
        });
        BaseOverthereFile spy = mock(BaseOverthereFile.class);
        when(spy.getConnection()).thenReturn((BaseOverthereConnection) otherConnection);
        tempFile.copyTo(spy);
        verify(spy, times(1)).copyFrom(tempFile);
    }

    protected static byte[] generateRandomBytes(final int size) {
        byte[] randomBytes = new byte[size];
        new Random().nextBytes(randomBytes);
        return randomBytes;
    }

}
