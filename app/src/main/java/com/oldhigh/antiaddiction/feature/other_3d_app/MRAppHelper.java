package com.oldhigh.antiaddiction.feature.other_3d_app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;

import com.oldhigh.antiaddiction.util.LogControl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MRAppHelper {

    private static final String TAG = MRAppHelper.class.getSimpleName();

    public static List<PackageInfo> getMRApps(Context context) {

        final List<PackageInfo> packageInfoList = getInstalledPackages(context);
        final ArrayList<PackageInfo> MRAppList = new ArrayList<>();
        for (PackageInfo packageInfo : packageInfoList) {
            if (MRAppHelper.isSystemApp(context, packageInfo.packageName)) {
                LogControl.d(" isSystemApp : " + packageInfo.packageName);
            } else {
                MRAppList.add(packageInfo);
            }
        }
        final PackageManager packageManager = context.getPackageManager();
        MRAppList.sort((p1, p2) -> {
            String label1 = p1.applicationInfo.loadLabel(packageManager).toString();
            String label2 = p2.applicationInfo.loadLabel(packageManager).toString();
            return label1.compareToIgnoreCase(label2); // 按字母顺序忽略大小写排序
        });
        LogControl.d(" MRAppList size : " + MRAppList.size());
        return MRAppList;
    }

    /**
     * 获取所有已经安装的 有  ACTION_MAIN 的应用
     */
    public static List<PackageInfo> getInstalledPackages(Context context) {

        List<PackageInfo> packageInfoList;
        HashSet<String> pkgList;
        PackageManager pm = context.getPackageManager();

        // 兼容
        int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        if (targetSdkVersion < 30) {
            packageInfoList = pm.getInstalledPackages(0);
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            List<ResolveInfo> infoList = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
            packageInfoList = new ArrayList<>();
            // 为了去重的集合
            pkgList = new HashSet<>();
            for (ResolveInfo info : infoList) {
                String packageName = info.activityInfo.packageName;
                if (!pkgList.contains(packageName)) {
                    try {
                        PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                        packageInfoList.add(packageInfo);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                pkgList.add(packageName);
            }
        }
        LogControl.d(TAG, "packageInfo size =  " + packageInfoList.size());

        return packageInfoList;
    }

    public static PackageInfo getPackageInfo(Context context, String packageName) {

        try {
            return context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 判断当前的应用是否使用了SDK
     *
     * @param packageName 应用的包名
     * @return true 表示使用了SDK
     */
    public static boolean isSystemApp(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 ||
                    (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;

        } catch (PackageManager.NameNotFoundException | ClassCastException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isServerApp(Context context, String packageName) {
        String serverType = "com.xreal.nrsdk.network.type";
        PackageManager pm = context.getPackageManager();
        try {
            final ServiceInfo serviceInfo = pm.getServiceInfo(
                    new ComponentName(packageName, "ai.nreal.framework.net.binder.Acceptor"), 0);
            final Bundle metaData = serviceInfo.metaData;
            if (metaData == null) {
                return false;
            }
            final String pkgValue = (String) metaData.get(serverType);
            LogControl.d(TAG, "isServerApp: pkgValue = " + pkgValue);
            return "server".equals(pkgValue);

        } catch (PackageManager.NameNotFoundException ignored) {

        }

        return false;
    }

    private static <T> T getMeta(Context context, String packageName, String key, T defaultValue) {


        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);

            Bundle metaData = ai.metaData;
            if (metaData == null) {
                return null;
            }
            Object val = metaData.get(key);
            if (val == null) {
                return defaultValue;
            }

            return (T) val;
        } catch (PackageManager.NameNotFoundException | ClassCastException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

}
