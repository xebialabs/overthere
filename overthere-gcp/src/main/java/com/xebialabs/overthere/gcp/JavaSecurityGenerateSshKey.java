package com.xebialabs.overthere.gcp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import org.apache.commons.codec.binary.Base64;

// TODO https://digitalai.atlassian.net/browse/ENG-4771
class JavaSecurityGenerateSshKey implements GenerateSshKey {

    private static String encodePrivateKey(PrivateKey privateKey) {
        return new StringBuilder("-----BEGIN RSA PRIVATE KEY-----")
                .append("\n")
                .append(new String(Base64.encodeBase64(privateKey.getEncoded())))
                .append("\n")
                .append("-----END RSA PRIVATE KEY-----")
                .append("\n")
                .toString();
    }

    private static String encodePublicKey(RSAPublicKey key, String username) throws IOException {
        return "ssh-rsa " + new String(Base64.encodeBase64(encodePublicKeyBytes(key)), StandardCharsets.UTF_8) + " " + username;
    }

    private static byte[] encodePublicKeyBytes(RSAPublicKey key) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        /* encode the "ssh-rsa" string */
        byte[] sshrsa = new byte[] {0, 0, 0, 7, 's', 's', 'h', '-', 'r', 's', 'a'};
        out.write(sshrsa);
        /* Encode the public exponent */
        BigInteger e = key.getPublicExponent();
        byte[] data = e.toByteArray();
        encodeUInt32(data.length, out);
        out.write(data);
        /* Encode the modulus */
        BigInteger m = key.getModulus();
        data = m.toByteArray();
        encodeUInt32(data.length, out);
        out.write(data);
        return out.toByteArray();
    }

    private static void encodeUInt32(int value, OutputStream out) throws IOException {
        byte[] tmp = new byte[4];
        tmp[0] = (byte)((value >>> 24) & 0xff);
        tmp[1] = (byte)((value >>> 16) & 0xff);
        tmp[2] = (byte)((value >>> 8) & 0xff);
        tmp[3] = (byte)(value & 0xff);
        out.write(tmp);
    }

    @Override
    public SshKeyPair generate(final String username, int keySize) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize);
            java.security.KeyPair key = keyGen.generateKeyPair();
            PrivateKey priv = key.getPrivate();
            PublicKey pub = key.getPublic();

            String privateKeyString = encodePrivateKey(priv);
            String publicKeyString = encodePublicKey((RSAPublicKey) pub, username);
            return new SshKeyPair(username, privateKeyString, publicKeyString, "");
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

}
