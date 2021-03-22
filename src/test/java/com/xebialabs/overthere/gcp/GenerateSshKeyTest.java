package com.xebialabs.overthere.gcp;

import java.io.StringReader;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.RSAPublicKeySpec;
import java.util.concurrent.ThreadLocalRandom;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.encoders.Base64;
import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class GenerateSshKeyTest {

    private static final String RSA_ALGORITHM = "RSA";

    private JCraftGenerateSshKey generateSshKey = new JCraftGenerateSshKey();

    @Test
    public void test() throws Exception {
        final String username = "test_username";

        SshKeyPair keyPair = generateSshKey.generate(username, 1024);

        assertThat(keyPair.getKeyUsername(), equalTo(username));
        assertThat(keyPair.getFingerPrint(), notNullValue());
        assertThat(keyPair.getPublicKey(), notNullValue());
        assertThat(keyPair.getPrivateKey(), notNullValue());

        // create a challenge
        byte[] challenge = new byte[10000];
        ThreadLocalRandom.current().nextBytes(challenge);

        // sign using the private key
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(getPrivateKey(keyPair.getPrivateKey()));
        sig.update(challenge);
        byte[] signature = sig.sign();

        // verify signature using the public key
        sig.initVerify(getPublicKey(keyPair.getPublicKey()));
        sig.update(challenge);

        assertThat(sig.verify(signature), equalTo(true));
    }

    private PrivateKey getPrivateKey(final String keyString) throws Exception {
        PrivateKey key;
        try (PEMParser pem = new PEMParser(new StringReader(keyString))) {
            JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();
            Object pemContent = pem.readObject();
            PEMKeyPair pemKeyPair = (PEMKeyPair) pemContent;
            KeyPair keyPair = jcaPEMKeyConverter.getKeyPair(pemKeyPair);
            key = keyPair.getPrivate();
        }
        return key;
    }

    private PublicKey getPublicKey(String publicKey) throws Exception {
        // format is <type><space><base64data><space><comment>
        String[] line = publicKey.trim().split(" ", 3);
        String type = line[0];
        String content = line[1];
        // String comment = line[2];

        ByteBuffer buf = ByteBuffer.wrap(Base64.decode(content));

        // format of decoded content is: <type><keyparams>
        // where type and each param is a DER string
        String decodedType = new String(readDERString(buf));
        if (!decodedType.equals(type)) {
            throw new IllegalArgumentException("expected " + type + ", got "
                    + decodedType);
        }
        // rsa key params are e, y
        BigInteger e = new BigInteger(readDERString(buf));
        BigInteger y = new BigInteger(readDERString(buf));
        return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(
                new RSAPublicKeySpec(y, e));
    }

    public static byte[] readDERString(ByteBuffer buf) {
        int length = buf.getInt();
        if (length > 8192) {
            throw new IllegalArgumentException("DER String Length " + length + " > 8192");
        }
        byte[] bytes = new byte[length];
        buf.get(bytes);
        return bytes;
    }
}
