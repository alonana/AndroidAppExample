package com.example.myapplication;

public class MyAppException extends RuntimeException {
    public MyAppException(String message) {
        super(message);
    }

    public MyAppException(Throwable e) {
        super(e);
    }
}
