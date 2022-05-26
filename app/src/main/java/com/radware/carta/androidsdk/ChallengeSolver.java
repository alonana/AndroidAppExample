package com.radware.carta.androidsdk;

import java.util.HashMap;

public class ChallengeSolver {
    private final String challenge;
    private final int difficulty;
    private long nonce;
    private long lastDebugTime;
    private int counter;

    public ChallengeSolver(String challenge, int difficulty) {
        this.challenge = BytesUtils.removeHexPrefix(challenge);
        this.difficulty = difficulty;
    }

    public HashMap<String, Object> solve() {
        BouncerLogger.debug("solving challenge difficulty " + this.difficulty);
        long startTime = System.currentTimeMillis();
        this.nonce = 0;
        this.counter = 0;
        while (!this.isSolved()) {
            this.nonce++;
            this.counter++;
        }
        BouncerLogger.debug("nonce solved: " + this.nonce);
        long passedMilliseconds = System.currentTimeMillis() - startTime;
        HashMap<String, Object> result = new HashMap<>();
        result.put("challenge", "0x" + this.challenge);
        result.put("nonce", "0x" + this.get16BytesNonceHex());
        result.put("workMilliseconds", passedMilliseconds);
        result.put("workCycles", this.nonce);
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
        if (this.counter > 1000) {
            this.counter = 0;
            long now = System.currentTimeMillis();
            if (now - this.lastDebugTime > 1000) {
                this.lastDebugTime = now;
                BouncerLogger.debug("nonce " + this.nonce + " zeros " + bitsCount);
            }
        }
        return bitsCount >= this.difficulty;
    }

}
