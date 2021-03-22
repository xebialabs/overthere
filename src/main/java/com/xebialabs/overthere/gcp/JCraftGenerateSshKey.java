package com.xebialabs.overthere.gcp;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

class JCraftGenerateSshKey implements GenerateSshKey {

    private final JSch jsch = new JSch();

    @Override
    public SshKeyPair generate(final String username, final int keySize) {
        try {
            KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA, keySize);

            ByteArrayOutputStream privateKeyOutputStream = new ByteArrayOutputStream();
            keyPair.writePrivateKey(privateKeyOutputStream);

            ByteArrayOutputStream publicKeyOutputStream = new ByteArrayOutputStream();
            keyPair.writePublicKey(publicKeyOutputStream, username);
            keyPair.dispose();
            return new SshKeyPair(
                    username,
                    new String(privateKeyOutputStream.toByteArray(), StandardCharsets.UTF_8),
                    new String(publicKeyOutputStream.toByteArray(), StandardCharsets.UTF_8),
                    keyPair.getFingerPrint()
            );
        } catch (JSchException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
