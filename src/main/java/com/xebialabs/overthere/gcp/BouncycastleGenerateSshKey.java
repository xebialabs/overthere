package com.xebialabs.overthere.gcp;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.jcajce.provider.asymmetric.util.PrimeCertaintyCalculator;
import org.bouncycastle.util.encoders.Base64;

// TODO https://digitalai.atlassian.net/browse/ENG-4771
class BouncycastleGenerateSshKey implements GenerateSshKey {

    @Override
    public SshKeyPair generate(final String username, int keySize) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(keySize);
            java.security.KeyPair key = keyGen.generateKeyPair();
            PrivateKey priv = key.getPrivate();
            PublicKey pub = key.getPublic();
            String privateKey = new String(Base64.encode(priv.getEncoded(), 0, priv.getEncoded().length));
            String publicKey1 = new String(Base64.encode(pub.getEncoded(), 0, pub.getEncoded().length));
            String publicKey = new String(Base64.encode(publicKey1.getBytes(), 0, publicKey1.getBytes().length));


            RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator();
            rsaKeyPairGenerator.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001),
                    CryptoServicesRegistrar.getSecureRandom(),
                    keySize,
                    PrimeCertaintyCalculator.getDefaultCertainty(keySize)));
            AsymmetricCipherKeyPair asymmetricCipherKeyPair = rsaKeyPairGenerator.generateKeyPair();
            PrivateKeyInfo pkInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(asymmetricCipherKeyPair.getPrivate());

            SubjectPublicKeyInfo info = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(asymmetricCipherKeyPair.getPublic());

            return new SshKeyPair(username, privateKey, publicKey, "");
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
