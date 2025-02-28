package com.xreal.evapro.toolsapp;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycleHelper.registerLifecycle(this);
    }
}
