package com.oldhigh.antiaddiction.service;

import android.app.Notification;
import android.graphics.Point;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.oldhigh.antiaddiction.action.OperationAction;
import com.oldhigh.antiaddiction.action.OtherAction;
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
            // 移除通知
            cancelNotification(sbn.getKey());
            hasMatch(title, text);
        }
    }

    private void hasMatch(CharSequence title, CharSequence text) {
        Log.e(TAG, "hasMatch: " + text);
        if (TextUtils.isEmpty(text)) {
            return;
        }

        final Set<String> stringSet = SPUtils.getInstance().getStringSet(SpKey.KEY_POINT_list);
        if (stringSet != null) {
            for (String s : stringSet) {
                if (text.toString().contains(SpKey.notification_common + s)) {
                    handlePoint(s);
                }
            }
        }
        handleOtherAction(title, text);

    }

    private void handleOtherAction(CharSequence title, CharSequence text) {
        Log.e(TAG, "handleOtherAction: " + title + "  " + text);
        if (!title.toString().contains(("老高"))) {
            return;
        }
        // [3条]老高: mm4
        String content;
        if (text.toString().contains("老高:")) {
            content = text.toString().split("老高:")[1].trim();
        } else {
            content = text.toString().trim();
        }
//        final String content = text.toString().substring(text.toString().indexOf("mm") + 2);
        Log.e(TAG, "handleOtherAction: content= " + content);

        switch (content) {
            case "1":
                OtherAction.unlockNow();
                OtherAction.screenshot();
                break;
            case "2":
                OtherAction.back();
                break;
            case "3":
                OtherAction.clearAll();
                break;
            case "4":
                OtherAction.goHome();
                break;
        }
        // 5:105,660
        if (content.contains("5:")) {
            final Point point = new Point();
            point.x = Integer.parseInt(content.substring(content.indexOf("5:") + 1, content.indexOf(",")));
            point.y = Integer.parseInt(content.substring(content.indexOf(",") + 1));
            OtherAction.byPoint(point);
            return;
        }
        // 6:登录
        if (content.contains("6:")) {
            final String substring = content.substring(content.indexOf("6:") + 1);
            OtherAction.byText(substring);
            return;
        }
        // 7:输入
        if (content.contains("7:")) {
            final String substring = content.substring(content.indexOf("7:") + 1);
            Log.e(TAG, "handleOtherAction: 777   " + substring);

            OtherAction.byEditTextFirst(substring);
            return;
        }
        // 8:hint,content
        if (content.contains("8:")) {
            final String hint = content.substring(content.indexOf("8:") + 1, content.indexOf(","));
            final String temp = content.substring(content.indexOf(",") + 1, content.length() - 1);
            OtherAction.byEditText(hint, temp);
            return;
        }
        if (content.contains("9")) {
            OtherAction.lockNow();
            return;
        }
        if (content.contains("10")) {
            OtherAction.unlockNow();
            return;
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
