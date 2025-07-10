package com.xreal.evapro.toolsapp.service;

import static com.xreal.evapro.toolsapp.note.NoteAct.KEY_NOTE_CONTENT;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.xreal.evapro.toolsapp.util.NotificationHelper;
import com.xreal.evapro.toolsapp.util.SPUtils;


public class NotificationService extends Service {

    private static final String TAG = NotificationService.class.getSimpleName();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final Notification notification = NotificationHelper.getInstance().getNotification(this, "无");

        NotificationHelper.getInstance().changeContent(SPUtils.getInstance().getString(KEY_NOTE_CONTENT));
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String hideInfo = intent.getStringExtra(NotificationHelper.HIDE_INFO);
        Log.e(TAG, "onStartCommand: " + hideInfo);
        if (TextUtils.isEmpty(hideInfo)) {
            NotificationHelper.getInstance().changeContent(SPUtils.getInstance().getString(KEY_NOTE_CONTENT));
        } else if (hideInfo.equals(NotificationHelper.HIDE_INFO)) {
            NotificationHelper.getInstance().changeContent(hideInfo);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy: ");
        stopForeground(true);
        stopForeground(Service.STOP_FOREGROUND_REMOVE);
        stopSelf();
    }
}
