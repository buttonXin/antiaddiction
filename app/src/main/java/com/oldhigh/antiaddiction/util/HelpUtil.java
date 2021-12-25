package com.oldhigh.antiaddiction.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.oldhigh.antiaddiction.bean.AppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HelpUtil {


    public static List<AppInfo> getPackages(Context context) {
        // 获取已经安装的所有应用, PackageInfo　系统类，包含应用信息
        List<AppInfo> list = new ArrayList<>();
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { //非系统应用
                // AppInfo 自定义类，包含应用信息
                AppInfo appInfo = new AppInfo();
                appInfo.appName =
                        packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();//获取应用名称
                appInfo.packageName = packageInfo.packageName; //获取应用包名，可用于卸载和启动应用
                appInfo.versionName = packageInfo.versionName;//获取应用版本名
                appInfo.versionCode = packageInfo.versionCode;//获取应用版本号
                appInfo.icon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());//获取应用图标
                System.out.println(appInfo.toString());
                list.add(appInfo);
            } else { // 系统应用

            }
        }
        return  list;
    }

    public static List<AppInfo> getPackages(Context context, Set<String> allAppPkgNames) {
        // 获取已经安装的所有应用, PackageInfo　系统类，包含应用信息
        List<AppInfo> list = new ArrayList<>();
        if(allAppPkgNames == null || allAppPkgNames.size() == 0){
            return list;
        }
        for (String pkgName : allAppPkgNames) {
            try {

                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(pkgName, 0);
                // AppInfo 自定义类，包含应用信息
                AppInfo appInfo = new AppInfo();
                appInfo.appName =
                        packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();//获取应用名称
                appInfo.packageName = packageInfo.packageName; //获取应用包名，可用于卸载和启动应用
                appInfo.versionName = packageInfo.versionName;//获取应用版本名
                appInfo.versionCode = packageInfo.versionCode;//获取应用版本号
                appInfo.icon = packageInfo.applicationInfo.loadIcon(context.getPackageManager());//获取应用图标
                System.out.println(appInfo.toString());
                list.add(appInfo);


            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        return  list;
    }

}
