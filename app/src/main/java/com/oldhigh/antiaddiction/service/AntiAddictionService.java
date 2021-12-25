package com.oldhigh.antiaddiction.service;

import android.accessibilityservice.AccessibilityService;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.hjq.toast.ToastUtils;
import com.oldhigh.antiaddiction.DataManager;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class AntiAddictionService extends AccessibilityService implements DataManager.PackageEventListener {

    public static final String TAG = AntiAddictionService.class.getName();

    private String currentPkgName = "";
    private String curAppName = "";
    private int countToast = 0;


    Timer timerPackage = new Timer();
    Timer timerToast = new Timer();
    TimerTask taskToast = new TimerTask() {
        @Override
        public void run() {
            if (countToast >= 3) {
                cancel();
                countToast = 0;
                currentPkgName = "";
            }
            countToast++;
            toast(curAppName + " 超时了,歇歇先");
        }
    };

    private PackageManager packageManager;
    private Set<String> stringSet;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.e(TAG, "onServiceConnected: ");
        packageManager = getPackageManager();
        stringSet = DataManager.get().getAppNames();

        DataManager.get().setPackageEventListener(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String packageName = event.getPackageName().toString();

        if (getPackageName().equals(packageName)) {
            return;
        }


        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            Log.e(TAG, "onAccessibilityEvent: " + packageName + "  " + (stringSet == null ? 0 : stringSet.size()));
            if (stringSet == null || stringSet.size() == 0) {
                return;
            }


            Log.e(TAG, "onAccessibilityEvent: " + packageName);
            if (currentPkgName.equals(packageName)) {
                return;
            }

            taskToast.cancel();
            countToast = 0;

            currentPkgName = packageName;
            if (!stringSet.contains(packageName)) {
                return;
            }

            timerPackage.cancel();
            timerToast.cancel();

            String appName = "";
            try {
                appName = packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager).toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            curAppName = appName;
            toast("打开了 " + appName);
            timerPackage = new Timer();
            timerPackage.schedule(new TimerTask() {
                @Override
                public void run() {
                    timerToast = new Timer();
                    taskToast = new TimerTask() {
                        @Override
                        public void run() {
                            if (countToast >= 3) {
                                cancel();
                                countToast = 0;
                                currentPkgName = "";
                                return;
                            }
                            countToast++;
                            toast(curAppName + " 超时了,歇歇先");
                        }
                    };
                    timerToast.schedule(taskToast, 0, 3000);
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

    @Override
    public void onPackageChanged(Set<String> stringSet) {
        this.stringSet = stringSet;
    }
}
