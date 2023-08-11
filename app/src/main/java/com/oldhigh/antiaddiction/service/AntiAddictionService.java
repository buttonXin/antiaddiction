package com.oldhigh.antiaddiction.service;

import android.accessibilityservice.AccessibilityService;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.oldhigh.antiaddiction.DataManager;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class AntiAddictionService extends AccessibilityService implements DataManager.PackageEventListener {

    public static final String TAG = AntiAddictionService.class.getName();


    private final long timeout = 30 * 60 * 1000;

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
            toast(curAppName + " 过度使用哦,快休息休息");
        }
    };

    private PackageManager packageManager;
    private Set<String> stringSet;

    private final List<String> skipList = Arrays.asList(
            "跳过5", "跳过4", "跳过3", "跳过2", "跳过1", "跳过",
            "跳过5s", "跳过4s", "跳过3s", "跳过2s", "跳过1s", "跳过",
            "5跳过", "4跳过", "3跳过", "2跳过", "1跳过", "跳过","确定",
            "跳过广告5", "跳过广告4", "跳过广告3", "跳过广告2", "跳过广告",
            "我知道了",
            "以后再说"
    );

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


        CharSequence eventPackageName = event.getPackageName();
        if (TextUtils.isEmpty(eventPackageName)) {
            return;
        }
        String packageName = eventPackageName.toString();

        if (getPackageName().equals(packageName)) {
            return;
        }


        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            return;
        }
//        if (stringSet != null && stringSet.contains(packageName)) {
//            return;
//        }

        _performActionPath(packageName);

        for (int i = 0; i < skipList.size(); i++) {
            _performAction(packageName,
                    rootInActiveWindow.findAccessibilityNodeInfosByText(
                            skipList.get(i)));
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

//            if (stringSet == null || stringSet.size() == 0) {
//                return;
//            }

            if (currentPkgName.equals(packageName)) {
                return;
            }

            taskToast.cancel();
            countToast = 0;

            currentPkgName = packageName;
            if (!stringSet.contains(packageName)) {
                return;
            }


            Log.e(TAG, "onAccessibilityEvent: " + packageName);

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
            }, timeout);

        }

    }

    int widthPixels, heightPixels;

    private void _performActionPath(String packageName) {
//        if (widthPixels == 0) {
//            widthPixels = Resources.getSystem().getDisplayMetrics().widthPixels;
//            heightPixels = Resources.getSystem().getDisplayMetrics().heightPixels;
//        }
//
//        Path mPath = new Path();//线性的path代表手势路径,点代表按下,封闭的没用
//        mPath.moveTo(500, 500);

    }

    private void _performAction(String packageName, List<AccessibilityNodeInfo> nodeInfos) {
        for (int i = 0; i < nodeInfos.size(); i++) {
            nodeInfos.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//            Log.e(TAG, "_performAction:packageName= " + packageName);
        }
    }


    private void toast(String text) {
//        ToastUtils.show(text);
    }


    @Override
    public void onInterrupt() {

    }

    @Override
    public void onPackageChanged(Set<String> stringSet) {
        this.stringSet = stringSet;
    }
}
