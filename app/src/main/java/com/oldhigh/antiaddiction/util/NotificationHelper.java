package com.oldhigh.antiaddiction.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.oldhigh.antiaddiction.HomeAct;


public class NotificationHelper {
    private static final String CHANNEL_ID = "channel_id";

    public static void sendNotification(Context context, String content) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8.0 及以上需要创建通知通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "通知渠道", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // 🔹 创建 PendingIntent 触发 BroadcastReceiver
        final Intent broadcastIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
//        Intent broadcastIntent = new Intent(context, HomeAct.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 🔔 **构建通知**
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // 通知图标
                .setContentTitle("测试通知")  // 标题
                .setContentText(content)  // 内容
                .setAutoCancel(true)  // 点击后自动取消通知
                .setContentIntent(pendingIntent);  // 设置点击事件

        // 发送通知
        notificationManager.notify(1, builder.build());
    }
}
