package com.radware.carta.androidsdk;

public class BouncerException extends RuntimeException {
    public BouncerException(String message) {
        super(message);
    }

    public BouncerException(Throwable e) {
        super(e);
    }
}
