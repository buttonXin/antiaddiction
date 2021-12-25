package com.oldhigh.antiaddiction;

import android.content.Context;
import android.content.SharedPreferences;

import com.oldhigh.antiaddiction.bean.AppInfo;
import com.oldhigh.antiaddiction.util.HelpUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataManager {

    private static DataManager instance;

    private Set<AppInfo> appInfoSets = new HashSet<>();
    private Context context;
    private SharedPreferences sharedPreferences;
    private PackageEventListener listener;


    private DataManager() {
    }

    public static DataManager get() {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("appInfo", Context.MODE_PRIVATE);
    }


    public void clear() {
        appInfoSets.clear();
        sharedPreferences.edit().clear().apply();
    }

    public boolean hasAppInfo() {
        return appInfoSets.size() != 0;
    }


    public void addAppInfo(AppInfo appInfo) {
        if (appInfoSets.contains(appInfo)) {
            return;
        }
        appInfoSets.add(appInfo);
    }

    public void removeAppInfo(AppInfo appInfo) {
        appInfoSets.remove(appInfo);
    }

    public List<AppInfo> getSaveApps() {
        if (appInfoSets.size() == 0) {
            Set<String> all = sharedPreferences.getStringSet("all", new HashSet<>());
            List<AppInfo> packages = HelpUtil.getPackages(context, all);
            appInfoSets = new HashSet<>(packages);
            return packages;
        }
        return new ArrayList<>(appInfoSets);
    }

    public Set<String> getAppNames() {
        return sharedPreferences.getStringSet("all", new HashSet<>());
    }

    public void saveAll() {
        Set<String> stringSet = new HashSet<>();
        for (AppInfo info : appInfoSets) {
            stringSet.add(info.packageName);
        }
        sharedPreferences.edit().putStringSet("all", stringSet).apply();

        if (listener != null) {
            listener.onPackageChanged(stringSet);
        }
    }

    public void setPackageEventListener(PackageEventListener listener) {
        this.listener = listener;
    }


    public interface PackageEventListener {
        void onPackageChanged(Set<String> stringSet);
    }

}
