package com.radware.carta.androidsdk;

import java.util.HashMap;

public class ServerResponse {

    private final String body;
    private final HashMap<String, String> headers;

    public ServerResponse(String body, HashMap<String,String>headers){
        this.body = body;
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }
}
