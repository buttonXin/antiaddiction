package com.oldhigh.antiaddiction.util;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.PowerManager;

import com.oldhigh.antiaddiction.App;

public class DeviceManagerUtil {


    public static void lockNow() {
        DevicePolicyManager dpm =
                (DevicePolicyManager) App.getInstance().getSystemService(Context.DEVICE_POLICY_SERVICE);
        dpm.lockNow();
    }

    public static void unlockNow() {
        final Context application = App.getInstance();
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
