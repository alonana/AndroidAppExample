package com.example.myapplication.bouncer;

import java.net.URL;
import java.util.HashMap;

public class UrlAccess {
    private final String url;
    private final HashMap<String, String> headers;
    private final String originalHost;
    private final String originalPath;

    public UrlAccess(String url, HashMap<String, String> headers) {
        try {
            this.url = url;
            this.headers = headers;

            URL parsedUrl = new URL(url);
            String urlHost = parsedUrl.getProtocol() + "://" + parsedUrl.getHost();
            if (parsedUrl.getPort() != -1) {
                urlHost += ":" + parsedUrl.getPort();
            }
            this.originalHost = urlHost;

            if (parsedUrl.getPath() == "") {
                this.originalPath = "/";
            } else {
                this.originalPath = parsedUrl.getPath();
            }
        } catch (Exception e) {
            throw new BouncerException(e);
        }
    }

    public String getUrl() {
        return url;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getOriginalHost() {
        return originalHost;
    }

    public String getOriginalPath() {
        return originalPath;
    }
}
