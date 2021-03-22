package com.xebialabs.overthere.gcp;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class GcpKeyManagerFactoryTest {

    private String credFile1;
    private String credFile2;

    @BeforeClass
    public void init() throws Exception {
        credFile1 = Thread.currentThread().getContextClassLoader().getResources("gcp/sa-key-ssh-account.json").nextElement().getFile();
        credFile2 = Thread.currentThread().getContextClassLoader().getResources("gcp/sa-key-ssh-account2.json").nextElement().getFile();
    }

    @Test
    public void canReuseNewGcpKeyManager() {
        GcpKeyManager gcpKeyManager1 = GcpKeyManagerFactory.create(credFile1);
        GcpKeyManager gcpKeyManager2 = GcpKeyManagerFactory.create(credFile1);

        assertThat(gcpKeyManager1, notNullValue());
        assertThat(gcpKeyManager2, equalTo(gcpKeyManager1));
    }

    @Test
    public void canCreateNewGcpKeyManagerForNewKeyFile() {
        GcpKeyManager gcpKeyManager1 = GcpKeyManagerFactory.create(credFile1);
        GcpKeyManager gcpKeyManager2 = GcpKeyManagerFactory.create(credFile2);

        assertThat(gcpKeyManager1, notNullValue());
        assertThat(gcpKeyManager2, notNullValue());
        assertThat(gcpKeyManager2, not(gcpKeyManager1));
    }
}
