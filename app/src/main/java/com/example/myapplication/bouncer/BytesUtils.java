package com.example.myapplication.bouncer;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class BytesUtils {

    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }


    public static byte[] sha256(byte[] textBytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(textBytes);
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xff & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String toBase64(String text) {
        try {
            return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    public static String removeHexPrefix(String hexString) {
        if (hexString.startsWith("0x")) {
            return hexString.substring(2);
        }

        return hexString;
    }

    private static int getBit(byte[] data, int pos) {
        int posByte = pos / 8;
        int posBit = pos % 8;
        byte valByte = data[posByte];
        return valByte >> (8 - (posBit + 1)) & 0x0001;
    }

    public static int countTrailingZeroBits(byte[] bytes) {
        int bitsCount = bytes.length * 8;
        int currentBit = bitsCount - 1;
        int zeroBitsCount = 0;
        while (currentBit > 0) {
            int bit = getBit(bytes, currentBit);
            if (bit == 1) {
                return zeroBitsCount;
            }
            currentBit--;
        }
        return zeroBitsCount;
    }


}
