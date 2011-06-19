package com.xebialabs.overthere.ssh;

import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;

/**
 * Always trusts the host, 
 */
class LaxKeyVerifier implements HostKeyVerifier {
    @Override
    public boolean verify(String hostname, int port, PublicKey key) {
        logger.debug("Trusting host {}:{}", hostname, port);
        return true;
    }

    private static final Logger logger = LoggerFactory.getLogger(LaxKeyVerifier.class);
}
