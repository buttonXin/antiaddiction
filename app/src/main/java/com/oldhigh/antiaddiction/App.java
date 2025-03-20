package com.oldhigh.antiaddiction;

import android.app.Application;

import com.hjq.toast.ToastStrategy;
import com.hjq.toast.ToastUtils;

public class App extends Application {

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ToastUtils.init(this, new ToastStrategy() {

        });
        DataManager.get().init(this);

    }

    public static App getInstance() {
        return instance;
    }
}
