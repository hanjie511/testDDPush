package com.example.testddpush;

import android.app.Application;
import android.content.Context;

import java.util.UUID;

import io.objectbox.BoxStore;

public class MyApplication extends Application {
    private static BoxStore boxStore;
    @Override
    public void onCreate() {
        super.onCreate();
        init(this);
    }
    public static void init(Context context){
        boxStore=MyObjectBox.builder().androidContext(context.getApplicationContext()).build();
    }
    public static BoxStore getBoxStore(){
       return boxStore;
    }
}
