package com.example.myapplication.bouncer;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;

public class Bouncer {

    private static final Bouncer myInstance = new Bouncer();

    public static Bouncer getInstance(){
        return myInstance;
    }

    private Bouncer(){

    }
    public void setContext(Context context) throws BouncerException{
        try {
            File file = new File(context.getFilesDir(), "bouncer_seed");
            if (!file.exists()) {
                FileOutputStream out = new FileOutputStream(file);
                out.write("aaa".getBytes());
                out.close();
            }

        }catch (Exception e){
            throw new BouncerException(e);
        }
    }
}
