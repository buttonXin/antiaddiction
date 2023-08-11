package com.oldhigh.antiaddiction;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.oldhigh.antiaddiction.bean.AppInfo;
import com.oldhigh.antiaddiction.util.HelpUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataManager {

    public static final String TAG = DataManager.class.getSimpleName();
    private static DataManager instance;

    private Set<AppInfo> appInfoSets = new HashSet<>();
    private Set<String> adNameSets;
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

        adNameSets = sharedPreferences.getStringSet("ads", new HashSet<>());
        Log.e(TAG, "init: " + adNameSets.toString());
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

    public void addSkipAd(String ad) {
        adNameSets.add(ad);
        sharedPreferences.edit().putStringSet("ads", adNameSets).apply();
    }

    public void removeSkipAd(String ad) {
        adNameSets.remove(ad);
        sharedPreferences.edit().putStringSet("ads", adNameSets).apply();
    }

    public void removeAllEditAd() {
        adNameSets.clear();
        sharedPreferences.edit().putStringSet("ads", adNameSets).apply();
    }

    public Set<String> getAllAdName() {
        List<String> strings = Arrays.asList(
                "跳过5", "跳过4", "跳过3", "跳过2", "跳过1", "跳过",
                "跳过5s", "跳过4s", "跳过3s", "跳过2s", "跳过1s", "跳过",
                "5跳过", "4跳过", "3跳过", "2跳过", "1跳过", "跳过",
                "跳过广告5", "跳过广告4", "跳过广告3", "跳过广告2", "跳过广告",
                "我知道了",
                "以后再说"
        );
        Set<String> result = new HashSet<>(strings);
        Set<String> adsSets = sharedPreferences.getStringSet("ads", adNameSets);
        result.addAll(adsSets);
        return result;
    }


    public void setPackageEventListener(PackageEventListener listener) {
        this.listener = listener;
    }


    public interface PackageEventListener {
        void onPackageChanged(Set<String> stringSet);
    }

}
