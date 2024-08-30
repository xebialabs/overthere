package com.xebialabs.overthere;

import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.xebialabs.overthere.ConnectionOptions.*;
import static com.xebialabs.overthere.OperatingSystemFamily.WINDOWS;
import static com.xebialabs.overthere.cifs.BaseCifsConnectionBuilder.CONNECTION_TYPE;
import static com.xebialabs.overthere.cifs.CifsConnectionType.WINRM_NATIVE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ConnectionOptionsUtilTest {
    private ConnectionOptions options;

    public static boolean onWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("windows");
    }

    @BeforeMethod
    public void setupOptions() {
        options = new ConnectionOptions();
        options.set(OPERATING_SYSTEM, WINDOWS);
        options.set(CONNECTION_TYPE, WINRM_NATIVE);
    }

    @Test
    public void testFixOptionsWithPowerShell() throws Exception {
        Process mockProcess = mock(Process.class);
        InputStream mockInputStream = new ByteArrayInputStream("PowerShell".getBytes());
        when(mockProcess.getInputStream()).thenReturn(mockInputStream);
        when(mockProcess.waitFor()).thenReturn(0);

        Runtime runtime = mock(Runtime.class);
        when(runtime.exec(anyString())).thenReturn(mockProcess);
        try (MockedStatic<Runtime> mockedRuntime = Mockito.mockStatic(Runtime.class)) {
            mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);

            ConnectionOptions fixedOptions = ConnectionOptionsUtil.fixOptions(options);
            assertEquals(fixedOptions.get(FILE_COPY_COMMAND_FOR_WINDOWS, FILE_COPY_COMMAND_FOR_WINDOWS_DEFAULT), "echo F|xcopy {0} {1} /y");
        }
    }

    @Test
    public void testFixOptionsWithCmd() throws Exception {
        // Mock the process to return "CMD"
        Process mockProcess = mock(Process.class);
        InputStream mockInputStream = new ByteArrayInputStream("CMD".getBytes());
        when(mockProcess.getInputStream()).thenReturn(mockInputStream);
        when(mockProcess.waitFor()).thenReturn(0);

        Runtime runtime = mock(Runtime.class);
        when(runtime.exec(anyString())).thenReturn(mockProcess);
        try (MockedStatic<Runtime> mockedRuntime = Mockito.mockStatic(Runtime.class)) {
            mockedRuntime.when(Runtime::getRuntime).thenReturn(runtime);

            ConnectionOptions fixedOptions = ConnectionOptionsUtil.fixOptions(options);
            assertEquals(fixedOptions.get(FILE_COPY_COMMAND_FOR_WINDOWS, FILE_COPY_COMMAND_FOR_WINDOWS_DEFAULT), "copy {0} {1} /y");
        }
    }
}
