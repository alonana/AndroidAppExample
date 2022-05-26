package com.radware.carta.androidsdk;

import android.content.Context;

import java.util.HashMap;

public class Bouncer {

    private static final Bouncer myInstance = new Bouncer();

    public static Bouncer getInstance() {
        return myInstance;
    }

    private Context context;
    private String admissionUrl;

    private Bouncer() {
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public String getAdmissionUrl() {
        return admissionUrl;
    }

    public void setAdmissionUrl(String admissionUrl) {
        this.admissionUrl = admissionUrl;
    }

    public ServerResponse sendGet(String url, HashMap<String, String> headers) {
        UrlAccess urlAccess = new UrlAccess(url, headers);
        Account account = new Account(urlAccess);
        return account.sendGet();
    }

    public ServerResponse sendPost(String url, HashMap<String, String> headers, String body) {
        UrlAccess urlAccess = new UrlAccess(url, headers);
        Account account = new Account(urlAccess);
        return account.sendPost(body);
    }
}
