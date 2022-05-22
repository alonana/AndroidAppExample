package com.example.myapplication;

import android.content.Context;

import java.io.File;

public class Bouncer {
    public Bouncer(Context context){
        File file = new File(context.getFilesDir(), "bouncer_seed");
    }
}
