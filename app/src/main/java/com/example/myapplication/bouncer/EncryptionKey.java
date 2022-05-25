package com.example.myapplication.bouncer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

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

            byte[] bytesPrivateKey = EncryptionApi.hexStringToByteArray(hexPrivateKey);
            byte[] bytesPublicKey = EncryptionApi.hexStringToByteArray(hexPublicKey);

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
        try {
            ECPublicKey ecPublicKey = (ECPublicKey) this.keyPair.getPublic();
            String x = EncryptionApi.bytesToHexString(ecPublicKey.getW().getAffineX().toByteArray());
            String y = EncryptionApi.bytesToHexString(ecPublicKey.getW().getAffineY().toByteArray());
            String addressString = "{" +
                    "\"X\":\"" + x + "\"," +
                    "\"Y\":\"" + y + "\"" +
                    "}";
            return EncryptionApi.toBase64(addressString);
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    public String exportKeyPair() {
        byte[] bytesPrivateKey = this.keyPair.getPrivate().getEncoded();
        byte[] bytesPublicKey = this.keyPair.getPublic().getEncoded();
        String hexPrivateKey = EncryptionApi.bytesToHexString(bytesPrivateKey);
        String hexPublicKey = EncryptionApi.bytesToHexString(bytesPublicKey);
        return hexPrivateKey + " " + hexPublicKey;
    }

    public String signAsHexString(String text) {
        try {
            Signature ecdsa = Signature.getInstance("SHA256withECDSA");

            ecdsa.initSign(this.keyPair.getPrivate());

            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            ecdsa.update(textBytes);

            byte[] signatureBytes = ecdsa.sign();
            return EncryptionApi.bytesToHexString(signatureBytes);
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    public void validate(String text, String signatureHex) {
        try {
            Signature ecdsa = Signature.getInstance("SHA256withECDSA");

            ecdsa.initVerify(this.keyPair.getPublic());

            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            ecdsa.update(textBytes);

            byte[] signatureBytes = EncryptionApi.hexStringToByteArray(signatureHex);

            if (!ecdsa.verify(signatureBytes)) {
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
