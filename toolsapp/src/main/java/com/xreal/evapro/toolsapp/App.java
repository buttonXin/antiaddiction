package com.xreal.evapro.toolsapp;

import android.app.Application;

import com.xreal.evapro.toolsapp.util.ActivityLifecycleHelper;

public class App extends Application {

    public static App instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ActivityLifecycleHelper.registerLifecycle(this);
    }
    public static App getInstance() {
        return instance;
    }
}
