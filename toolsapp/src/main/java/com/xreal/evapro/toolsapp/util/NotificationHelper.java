package com.xreal.evapro.toolsapp.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.xreal.evapro.toolsapp.App;
import com.xreal.evapro.toolsapp.R;
import com.xreal.evapro.toolsapp.note.NoteAct;
import com.xreal.evapro.toolsapp.service.NotificationService;


public class NotificationHelper {
    private static final String CHANNEL_ID = "channel_id";

    private volatile static NotificationHelper sInstance = null;
    private Context mContext; // 添加Context成员变量
    private NotificationManager mNotificationManager; // 添加NotificationManager成员变量
    private Notification.Builder notificationBuilder;

    private NotificationHelper() {
    }

    public static NotificationHelper getInstance() {
        if (sInstance == null) {
            synchronized (NotificationHelper.class) {
                if (sInstance == null) {
                    sInstance = new NotificationHelper();
                }
            }
        }
        return sInstance;
    }


    public Notification getNotification(Context context, String content) {
        mContext = context; // 保存Context引用
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 及以上需要创建通知通道
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "olTools", NotificationManager.IMPORTANCE_DEFAULT);
        // 这里设置整个通知渠道为 静音模式
        channel.setSound(null, null);
        channel.enableVibration(false);
        channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(channel);

        // 🔹 创建 PendingIntent 触发 BroadcastReceiver
        Intent broadcastIntent = new Intent(context, NoteAct.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔔 **构建通知**

        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)  // 通知图标
                .setContentTitle("标题")  // 标题
//                .setContentText(content)  // 内容
                .setAutoCancel(true)  // 点击后自动取消通知
                .setPriority(Notification.PRIORITY_LOW)  // 设置通知优先级为最低
                .setContentIntent(pendingIntent);  // 设置点击事件
        notificationBuilder = builder;

        // 发送通知
        return builder.build();
    }

    /**
     * 更新通知栏的内容
     * 更新时, space空间会有通知的声音 ,这个需要修改一个更新的频率!!!
     */
    public void changeContent(String content) {

        if (notificationBuilder == null || mContext == null || mNotificationManager == null) {
            return;
        }

        // 更新通知内容
        notificationBuilder.setContentTitle(content);

        // 使用相同的ID更新通知
        mNotificationManager.notify(1, notificationBuilder.build());

    }

    public void show() {
        final NotificationManager notificationManager = (NotificationManager) App.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!notificationManager.areNotificationsEnabled()) {
            // 打开通知
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", App.getInstance().getPackageName());
            intent.putExtra("app_uid", App.getInstance().getApplicationInfo().uid);
            intent.putExtra("android.provider.extra.APP_PACKAGE", App.getInstance().getPackageName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            App.getInstance().startActivity(intent);
            return;
        }
        App.getInstance().startForegroundService(new Intent(App.getInstance(), NotificationService.class));
    }

    public void hide() {
        App.getInstance().stopService(new Intent(App.getInstance(), NotificationService.class));
    }


}
