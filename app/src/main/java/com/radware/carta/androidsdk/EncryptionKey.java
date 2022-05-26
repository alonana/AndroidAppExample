package com.radware.carta.androidsdk;

import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class EncryptionKey {

    static {
        Security.removeProvider("BC");//first remove default os provider
        // this is working even it appears in the studio as compile error
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        Security.insertProviderAt(bouncyCastleProvider, 1);//add new provider
    }

    private final KeyPair keyPair;
    private final String address;

    public EncryptionKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            keyGen.initialize(new ECGenParameterSpec("secp256k1"), new SecureRandom());
            this.keyPair = keyGen.generateKeyPair();
            this.address = this.calculateAddress();
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    public EncryptionKey(String exportedKeyPair) {
        try {
            String[] hexKeys = exportedKeyPair.split(" ");

            String hexPrivateKey = hexKeys[0];
            String hexPublicKey = hexKeys[1];

            byte[] bytesPrivateKey = BytesUtils.hexStringToByteArray(hexPrivateKey);
            byte[] bytesPublicKey = BytesUtils.hexStringToByteArray(hexPublicKey);

            PKCS8EncodedKeySpec specPrivateKey = new PKCS8EncodedKeySpec(bytesPrivateKey);
            X509EncodedKeySpec specPublicKey = new X509EncodedKeySpec(bytesPublicKey);

            KeyFactory factory = KeyFactory.getInstance("ECDSA");

            PrivateKey privateKey = factory.generatePrivate(specPrivateKey);
            PublicKey publicKey = factory.generatePublic(specPublicKey);

            this.keyPair = new KeyPair(publicKey, privateKey);
            this.address = this.calculateAddress();
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    private String calculateAddress() {
        ECPublicKey ecPublicKey = (ECPublicKey) this.keyPair.getPublic();
        return BytesUtils.bigIntsToBase64(
                "X", "Y",
                ecPublicKey.getW().getAffineX(),
                ecPublicKey.getW().getAffineY()
        );
    }

    public String exportKeyPair() {
        byte[] bytesPrivateKey = this.keyPair.getPrivate().getEncoded();
        byte[] bytesPublicKey = this.keyPair.getPublic().getEncoded();
        String hexPrivateKey = BytesUtils.bytesToHexString(bytesPrivateKey);
        String hexPublicKey = BytesUtils.bytesToHexString(bytesPublicKey);
        return hexPrivateKey + " " + hexPublicKey;
    }

    public String sign(String text) {
        try {
            Signature ecdsa = Signature.getInstance("SHA256withECDSA");

            ecdsa.initSign(this.keyPair.getPrivate());

            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            ecdsa.update(textBytes);

            byte[] signatureBytes = ecdsa.sign();
            ECPublicKey publicKey = (ECPublicKey) this.keyPair.getPublic();
            BigInteger order = publicKey.getParams().getOrder();
            BigInteger[] bigInts = StandardDSAEncoding.INSTANCE.decode(order, signatureBytes);
            BigInteger r = bigInts[0];
            BigInteger s = bigInts[1];
            return BytesUtils.bigIntsToBase64("r", "s", r, s);
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }


    public void validate(String text, String signature) {
        try {
            Signature ecdsa = Signature.getInstance("SHA256withECDSA");

            ecdsa.initVerify(this.keyPair.getPublic());

            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            ecdsa.update(textBytes);

            BigInteger[] bigInts =BytesUtils.base64ToBigInts("r","s",signature);
            BigInteger r = bigInts[0];
            BigInteger s = bigInts[1];
            ECPublicKey publicKey = (ECPublicKey) this.keyPair.getPublic();
            BigInteger order = publicKey.getParams().getOrder();
            byte[] bytes = StandardDSAEncoding.INSTANCE.encode(order, r,s);

            if (!ecdsa.verify(bytes)) {
                throw new BouncerException("verify failed");
            }
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    public String getAddress() {
        return address;
    }
}
