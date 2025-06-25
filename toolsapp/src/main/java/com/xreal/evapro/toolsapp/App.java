package com.xreal.evapro.toolsapp;

import android.app.Application;

import com.xreal.evapro.toolsapp.bean.NoteBean;
import com.xreal.evapro.toolsapp.util.ActivityLifecycleHelper;

import ai.nreal.common.nrealdatabase.db.DBManager;

public class App extends Application {

    public static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        ActivityLifecycleHelper.registerLifecycle(this);
        db();
    }

    private void db() {
        DBManager.init(this, "note.db");
        DBManager.getInstance().create(NoteBean.class);
        DBManager.getInstance().alter(NoteBean.class);

//        final List<NoteBean> noteBeans = DBManager.getInstance().get(NoteBean.class);
    }

    public static App getInstance() {
        return instance;
    }
}
