package com.example.myapplication.bouncer;

import java.util.HashMap;

public class ChallengeSolver {
    private final String challenge;
    private final int difficulty;
    private long nonce;

    public ChallengeSolver(String challenge, int difficulty) {
        this.challenge = BytesUtils.removeHexPrefix(challenge);
        this.difficulty = difficulty;
    }

    public HashMap<String, String> solve() {
        long startTime = System.currentTimeMillis();
        this.nonce = 0;
        while (!this.isSolved()) {
            this.nonce++;
        }
        long passedMilliseconds = System.currentTimeMillis() - startTime;
        HashMap<String, String> result = new HashMap<>();
        result.put("challenge", this.challenge);
        result.put("nonce", this.get16BytesNonceHex());
        result.put("workMilliseconds", Long.toString(passedMilliseconds));
        result.put("workCycles", Long.toString(this.nonce));
        return result;
    }

    private String get16BytesNonceHex() {
        String formattedNonce = Long.toString(this.nonce, 16);
        formattedNonce = BytesUtils.removeHexPrefix(formattedNonce);
        formattedNonce = "0000000000000000" + formattedNonce;
        return formattedNonce.substring(formattedNonce.length() - 16);
    }

    private boolean isSolved() {
        byte[] challengeBytes = BytesUtils.hexStringToByteArray(this.challenge);
        byte[] nonceBytes = BytesUtils.hexStringToByteArray(this.get16BytesNonceHex());

        byte[] combined = new byte[challengeBytes.length + nonceBytes.length];
        System.arraycopy(challengeBytes, 0, combined, 0, challengeBytes.length);
        System.arraycopy(nonceBytes, 0, combined, challengeBytes.length, nonceBytes.length);

        byte[] hashed = BytesUtils.sha256(combined);
        int bitsCount = BytesUtils.countTrailingZeroBits(hashed);
        return bitsCount >= this.difficulty;
    }

}
