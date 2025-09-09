package com.oldhigh.antiaddiction.action;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.ven.assists.AssistsCore;
import com.ven.assists.service.AssistsService;
import com.ven.assists.stepper.Step;
import com.ven.assists.stepper.StepCollector;
import com.ven.assists.stepper.StepImpl;

public class MiGuAction extends StepImpl {
    private static final String TAG = MiGuAction.class.getSimpleName();

    @Override
    public void onImpl(@NonNull StepCollector stepCollector) {
        stepCollector.next(1, true, (step, continuation) -> {

//            final Context application = AssistsService.Companion.getInstance().getApplicationContext();
//            final Intent intent = application.getPackageManager().getLaunchIntentForPackage("com.cmcc.cmvideo");
//
//            // 创建一个Intent来启动桌面
//            Intent startMain = new Intent(Intent.ACTION_MAIN);
//            startMain.addCategory(Intent.CATEGORY_HOME);
//            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//            // 启动桌面
//            application.startActivity(startMain);
            Log.e(TAG, "onImpl: 1 ");

            unlockNow();


            return Step.Companion.get(2, MiGuAction.class, null, 10000);
        }).next(2, true, (step, continuation) -> {
            Log.e(TAG, "onImpl: 2 ");
            final Context application = AssistsService.Companion.getInstance().getApplicationContext();
            final Intent intent = application.getPackageManager().getLaunchIntentForPackage("com.cmcc.cmvideo");

            // 启动桌面
            application.startActivity(intent);

            return Step.Companion.get(3, MiGuAction.class, null, 10000);
        }).next(3, true, (step, continuation) -> {
            Log.e(TAG, "onImpl: 3 ");
            AssistsCore.INSTANCE.findByText("国足",null,null,null).forEach(node -> {
                node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            });
            return Step.Companion.get(4, MiGuAction.class, null, 10000);
        }).next(4, true, (step, continuation) -> {
            Log.e(TAG, "onImpl: 3 ");
            lockNow();
            return Step.Companion.getNone();
        });
    }

    public void lockNow() {
        final Context application = AssistsService.Companion.getInstance().getApplicationContext();
        DevicePolicyManager dpm =
                (DevicePolicyManager) application.getSystemService(Context.DEVICE_POLICY_SERVICE);
        dpm.lockNow();
    }

    public void unlockNow() {
        final Context application = AssistsService.Companion.getInstance().getApplicationContext();
        DevicePolicyManager dpm =
                (DevicePolicyManager) application.getSystemService(Context.DEVICE_POLICY_SERVICE);

        //解锁
        try {
            KeyguardManager km = (KeyguardManager) application.getSystemService(Context.KEYGUARD_SERVICE);
            final KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unLock");
            kl.disableKeyguard();

            PowerManager mgr = (PowerManager) application.getSystemService(Context.POWER_SERVICE);
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock wl = mgr.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            wl.acquire(6000);
            wl.release();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
