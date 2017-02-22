package com.xebialabs.overthere;


import com.xebialabs.overthere.ssh.SshConnectionBuilder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class ConnectionOptionsTest {

    @DataProvider(name = "secretKeys")
    public Object[][] secretKeys() {
        return new Object[][]{
                {ConnectionOptions.PASSWORD},
                {SshConnectionBuilder.PASSPHRASE},
                {SshConnectionBuilder.PRIVATE_KEY},
                {SshConnectionBuilder.SU_PASSWORD}
        };
    }

    @Test(dataProvider = "secretKeys")
    public void shouldHidePasswordInToString(String key) {
        ConnectionOptions connectionOptions = new ConnectionOptions();
        connectionOptions.set(key, "$ecret");
        assertThat(connectionOptions.toString(), not(containsString("$ecret")));
    }
}
