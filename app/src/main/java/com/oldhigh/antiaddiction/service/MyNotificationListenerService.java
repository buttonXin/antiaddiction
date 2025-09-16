package com.oldhigh.antiaddiction.service;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.oldhigh.antiaddiction.action.OperationAction;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.oldhigh.antiaddiction.util.SPUtils;
import com.ven.assists.stepper.StepManager;

import java.util.Set;


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
            hasMatch(title, sbn);
            hasMatch(text, sbn);
        }
    }

    private void hasMatch(CharSequence text, StatusBarNotification sbn) {
        Log.e(TAG, "hasMatch: " + text);
        if (TextUtils.isEmpty(text)) {
            return;
        }
        final Set<String> stringSet = SPUtils.getInstance().getStringSet(SpKey.KEY_POINT_list);
        if (stringSet != null) {
            for (String s : stringSet) {
                if (text.toString().contains(SpKey.notification_common + s)) {
                    // 移除通知
                    cancelNotification(sbn.getKey());
                    handlePoint(s);
                }
            }
        }

    }

    private void handlePoint(String s) {
        final boolean aBoolean = SPUtils.getInstance().getBoolean(SpKey.KEY_Notification);
        if (!aBoolean) {
            return;
        }
        StepManager.INSTANCE.execute(OperationAction.class, 1, 100, s, true);


    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "通知被移除 - 包名: " + sbn.getPackageName());
    }
}
