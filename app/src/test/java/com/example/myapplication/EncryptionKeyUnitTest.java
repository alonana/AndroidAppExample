package com.example.myapplication;

import com.radware.carta.androidsdk.EncryptionKey;

import org.junit.Test;

public class EncryptionKeyUnitTest {
    @Test
    public void test() {
        EncryptionKey key = new EncryptionKey();
        String text = "Hello World! My name is Indigo Montoya. You have killed my father, prepare to die!";
        String signature = key.sign(text);
        System.out.println(signature);
        key.validate(text, signature);

        String exportKeyPair = key.exportKeyPair();
        EncryptionKey importedKey = new EncryptionKey(exportKeyPair);
        importedKey.validate(text, signature);
    }
}