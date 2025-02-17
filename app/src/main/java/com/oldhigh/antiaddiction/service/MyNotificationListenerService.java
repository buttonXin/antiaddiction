package com.oldhigh.antiaddiction.service;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.oldhigh.antiaddiction.util.AudioPlayer;
import com.oldhigh.antiaddiction.util.NotificationHelper;

public class MyNotificationListenerService extends NotificationListenerService {
    private static final String TAG = MyNotificationListenerService.class.getSimpleName();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        // 获取通知的包名
        String packageName = sbn.getPackageName();

        // 获取通知内容
        Notification notification = sbn.getNotification();
        if (notification != null && notification.extras != null) {
            CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);

            Log.d(TAG, "收到通知 - 包名: " + packageName);
            Log.d(TAG, "标题: " + title);
            Log.d(TAG, "内容: " + text);
            hasMatch(title);
            hasMatch(text);
        }
    }

    private void hasMatch(CharSequence text) {
        Log.e(TAG, "hasMatch: " + text);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        if (text.toString().contains("低于80%")) {
            playAudio();
        } if (text.toString().contains("123123")) {
            playAudio();
        }
    }

    private void playAudio() {
        AudioPlayer.getInstance().playFromAssets(this, "虞美人-熙宝.mp3");
        NotificationHelper.sendNotification(this, "点击关闭音乐播放");
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "通知被移除 - 包名: " + sbn.getPackageName());
    }
}
