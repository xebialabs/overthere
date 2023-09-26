package com.xebialabs.overthere.cifs;

import org.testng.annotations.Test;

import javax.net.ssl.HostnameVerifier;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class JumpstationAwareHostnameVerifierTest {
    @Test
    public void testVerifyWithRemoteHost() {
        String remoteHost = "remote.host.com";
        HostnameVerifier hostnameVerifier = (host, session) -> host.equals(remoteHost);
        JumpstationAwareHostnameVerifier verifier = new JumpstationAwareHostnameVerifier(remoteHost, hostnameVerifier);
        boolean result = verifier.verify("localhost", null);
        assertTrue(result, "Hostname verification should pass for the remote host");
    }

    @Test
    public void testVerifyWithUnknownHost() {
        String remoteHost = "remote.host.com";
        String unknownHost = "unknown.host.com";
        HostnameVerifier hostnameVerifier = (host, session) -> host.equals(remoteHost);
        JumpstationAwareHostnameVerifier verifier = new JumpstationAwareHostnameVerifier(unknownHost, hostnameVerifier);
        boolean result = verifier.verify("localhost", null);
        assertFalse(result, "Hostname verification should fail for the unknown host");
    }

    @Test
    public void testVerifyWithNoopVerifier() {
        String remoteHost = "remote.host.com";
        String unknownHost = "unknown.host.com";
        HostnameVerifier hostnameVerifier = (host, session) -> true;
        JumpstationAwareHostnameVerifier verifier = new JumpstationAwareHostnameVerifier(unknownHost, hostnameVerifier);
        boolean result = verifier.verify("localhost", null);
        assertTrue(result, "if we use NoopHostname verifier hostname verification should pass for the unknown host");
    }
}
