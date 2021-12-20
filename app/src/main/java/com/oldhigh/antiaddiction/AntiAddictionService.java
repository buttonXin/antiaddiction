package com.oldhigh.antiaddiction;

import android.accessibilityservice.AccessibilityService;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.hjq.toast.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

public class AntiAddictionService extends AccessibilityService {

    public static final String TAG = AntiAddictionService.class.getName();

    private String currentPkgName = "";
    private String curAppName = "";

    Timer timer = new Timer();

    private PackageManager packageManager;

    @Override
    public void onCreate() {
        super.onCreate();

        packageManager = getPackageManager();

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName().toString();

        if (getPackageName().equals(packageName)) {
            return;
        }


        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Log.e(TAG, "onAccessibilityEvent: " + packageName);
            if (currentPkgName.equals(packageName)) {
                return;
            } else {
                currentPkgName = packageName;
                timer.cancel();
            }


            String appName = "";
            try {
                appName = packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager).toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            curAppName = appName;
            toast("打开了 " + appName + ",开始计时");

            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    toast(curAppName + " 超时了");
                }
            }, 10 * 1000);

        }

    }

    private void toast(String text) {
        ToastUtils.show(text);
    }


    @Override
    public void onInterrupt() {

    }
}
