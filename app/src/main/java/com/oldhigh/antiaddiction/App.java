package com.oldhigh.antiaddiction;

import android.app.Application;

import com.hjq.toast.ToastStrategy;
import com.hjq.toast.ToastUtils;
import com.oldhigh.antiaddiction.activity.ActivityLifecycleHelper;

public class App extends Application {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ToastUtils.init(this, new ToastStrategy() {

        });

        ActivityLifecycleHelper.registerLifecycle(this);
    }

    public static App getInstance() {
        return instance;
    }
}
