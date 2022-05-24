package com.example.myapplication.bouncer;

public class BouncerException extends RuntimeException {
    public BouncerException(String message) {
        super(message);
    }

    public BouncerException(Throwable e) {
        super(e);
    }
}
