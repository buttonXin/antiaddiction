package com.oldhigh.antiaddiction.receiver;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.oldhigh.antiaddiction.util.AudioPlayer;
import com.oldhigh.antiaddiction.util.NotificationHelper;

public class SmsReceiver extends BroadcastReceiver {
//    private static MessageListener mMessageListener;

    private static final String TAG = SmsReceiver.class.getSimpleName();
    private Context mContext;
    public static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    public static final String SMS_DELIVER_ACTION = "android.provider.Telephony.SMS_DELIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;

        Log.e(TAG, "Received a SMS notification");
        String action = intent.getAction();
        if (SMS_RECEIVED_ACTION.equals(action) || SMS_DELIVER_ACTION.equals(action)) {

            Log.e(TAG, "开始接收短信.....");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null && pdus.length > 0) {
//                    SmsMessage[] messages = new SmsMessage[pdus.length];
//                    for (int i = 0; i < pdus.length; i++) {
//                        byte[] pdu = (byte[]) pdus[i];
//                        messages[i] = SmsMessage.createFromPdu(pdu);
//                    }
//                    for (SmsMessage message : messages) {
//                        String content = message.getMessageBody();// 得到短信内容
//                        String sender = message.getOriginatingAddress();// 得到发信息的号码
//                        Log.d(TAG, "111收到短信 - 发送者: " + sender + " , 内容: " + content);
//                    }

                    for (Object pdu : pdus) {
                        SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = message.getDisplayOriginatingAddress();  // 发送者号码
                        String messageBody = message.getMessageBody();  // 短信内容

                        SharedPreferences sp = context.getSharedPreferences("data", MODE_PRIVATE);
                        final String string = sp.getString("key", "告警阈值");
                        Log.e(TAG, "hasMatch: " + string);
                        if (messageBody.contains(string)) {
                            playAudio(context);
                        }
//                        Log.d(TAG, "222收到短信 - 发送者: " + sender + " , 内容: " + messageBody);

                    }
                }

            }
        }
    }

    private void playAudio(Context context) {
        AudioPlayer.getInstance().playFromAssets(context, "虞美人-熙宝.mp3");
        NotificationHelper.sendNotification(context, "点击关闭音乐播放");
    }
}
