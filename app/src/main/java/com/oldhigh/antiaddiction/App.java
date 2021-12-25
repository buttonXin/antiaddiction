package com.oldhigh.antiaddiction;

import android.app.Application;

import com.hjq.toast.ToastStrategy;
import com.hjq.toast.ToastUtils;

public class App  extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        ToastUtils.init(this,new ToastStrategy(){

        });
        DataManager.get().init(this);

    }
}
