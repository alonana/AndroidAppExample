package com.radware.carta.androidsdk;

import org.json.JSONObject;

import java.math.BigInteger;
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
            byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(textBytes);
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    public static String fromBase64(String base64Data) {
        try {
            byte[] textBytes = Base64.getDecoder().decode(base64Data);
            return new String(textBytes, StandardCharsets.UTF_8);
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
            zeroBitsCount++;
            currentBit--;
        }
        return zeroBitsCount;
    }


    public static String bigIntsToBase64(String name1, String name2, BigInteger i1, BigInteger i2) {
        try {
            byte[] i1Bytes = i1.toByteArray();
            byte[] i2Bytes = i2.toByteArray();
            String i1Hex = bytesToHexString(i1Bytes);
            String i2Hex = bytesToHexString(i2Bytes);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(name1, i1Hex);
            jsonObject.put(name2, i2Hex);
            String jsonData = jsonObject.toString();
            return toBase64(jsonData);
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    public static BigInteger[] base64ToBigInts(String name1, String name2, String base64Data) {
        try {
            String jsonData = fromBase64(base64Data);
            JSONObject jsonObject = new JSONObject(jsonData);
            String i1Hex = jsonObject.getString(name1);
            String i2Hex = jsonObject.getString(name2);
            byte[] i1Bytes = hexStringToByteArray(i1Hex);
            byte[] i2Bytes = hexStringToByteArray(i2Hex);
            BigInteger i1 = new BigInteger(i1Bytes);
            BigInteger i2 = new BigInteger(i2Bytes);
            return new BigInteger[]{i1, i2};
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

}
