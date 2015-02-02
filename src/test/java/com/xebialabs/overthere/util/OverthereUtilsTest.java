package com.xebialabs.overthere.util;

import com.xebialabs.overthere.ConnectionOptions;
import com.xebialabs.overthere.OverthereFile;
import com.xebialabs.overthere.itest.OverthereConnectionItestBase;
import com.xebialabs.overthere.local.LocalConnection;
import com.xebialabs.overthere.local.LocalFile;
import org.testng.annotations.Test;

import static com.xebialabs.overthere.ConnectionOptions.TEMPORARY_DIRECTORY_PATH;
import static com.xebialabs.overthere.local.LocalConnection.LOCAL_PROTOCOL;
import static org.testng.Assert.*;

/**
 * Created by aalbul on 2/2/15.
 */
public class OverthereUtilsTest extends OverthereConnectionItestBase {
    @Override
    protected String getProtocol() {
        return LOCAL_PROTOCOL;
    }

    @Override
    protected ConnectionOptions getOptions() {
        ConnectionOptions options = new ConnectionOptions();
        options.set(TEMPORARY_DIRECTORY_PATH, temp.getRoot().getPath());
        return options;
    }

    @Override
    protected String getExpectedConnectionClassName() {
        return LocalConnection.class.getName();
    }

    @Test
    public void shouldGenerateUniqueTempFile() {
        OverthereFile base = LocalFile.valueOf(temp.getRoot());
        OverthereFile fileOne = OverthereUtils.getUniqueTempFile(base, "testFile");
        OverthereFile fileTwo = OverthereUtils.getUniqueTempFile(base, "testFile");
        assertFalse(fileOne.exists());
        assertFalse(fileTwo.exists());
        assertNotEquals(fileOne.getPath(), fileTwo.getPath());
    }
}
