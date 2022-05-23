package com.example.myapplication.bouncer;

import android.content.Context;

import java.util.HashMap;

public class Bouncer {

    private static final Bouncer myInstance = new Bouncer();

    public static Bouncer getInstance() {
        return myInstance;
    }

    private Context context;

    private Bouncer() {
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ServerResponse sendGet(String url, HashMap<String,String> headers){
        UrlAccess urlAccess = new UrlAccess(url,headers);
        Account account = new Account(urlAccess);
        return account.sendGet();
    }

    public ServerResponse sendPost(String url, HashMap<String,String> headers,String body){
        UrlAccess urlAccess = new UrlAccess(url,headers);
        Account account = new Account(urlAccess);
        return account.sendPost(body);
    }
//        try {
//            File file = new File(context.getFilesDir(), "bouncer_seed");
//            if (!file.exists()) {
//                FileOutputStream out = new FileOutputStream(file);
//                out.write("aaa".getBytes());
//                out.close();
//            }
//
//        }catch (Exception e){
//            throw new BouncerException(e);
//        }
}
