package com.xebialabs.overthere.spi;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.common.io.OutputSupplier;

import com.xebialabs.overthere.*;

import static com.google.common.io.ByteStreams.write;
import static com.xebialabs.overthere.ConnectionOptions.OPERATING_SYSTEM;
import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.OperatingSystemFamily.getLocalHostOperatingSystemFamily;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static org.mockito.Mockito.*;

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
        verify(spy, times(1)).localCopyFrom(tempFile);
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
