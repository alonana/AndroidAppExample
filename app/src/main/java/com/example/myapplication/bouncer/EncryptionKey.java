package com.example.myapplication.bouncer;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;

public class EncryptionKey {

    public EncryptionKey(String seedHex) {
        byte[] seedBytes = EncryptionApi.hexStringToByteArray(seedHex);
//        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
//
//        keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());
//
//        KeyPair pair = keyGen.generateKeyPair();
//        PrivateKey priv = pair.getPrivate();
//        PublicKey pub = pair.getPublic();
//
//        /*
//         * Create a Signature object and initialize it with the private key
//         */
//
//        Signature ecdsa = Signature.getInstance("SHA256withECDSA");
//
//        ecdsa.initSign(priv);
//
//        String str = "This is string to sign";
//        byte[] strByte = str.getBytes("UTF-8");
//        ecdsa.update(strByte);
//
//        /*
//         * Now that all the data to be signed has been read in, generate a
//         * signature for it
//         */
//
//        byte[] realSig = ecdsa.sign();
//        System.out.println("Signature: " + new BigInteger(1, realSig).toString(16));
//

    }
}
