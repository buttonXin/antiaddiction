package com.oldhigh.antiaddiction;

import android.app.Application;

import com.hjq.toast.ToastUtils;

public class App  extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this);
    }
}
