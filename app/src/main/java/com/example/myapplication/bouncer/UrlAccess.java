package com.example.myapplication.bouncer;

import java.util.HashMap;

public class UrlAccess {
    private final String url;
    private final HashMap<String, String> headers;

    public UrlAccess(String url, HashMap<String,String>headers){
        this.url = url;
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }
}
