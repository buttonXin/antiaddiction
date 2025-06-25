package com.xreal.evapro.toolsapp.local_server;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.xreal.evapro.toolsapp.BuildConfig;
import com.xreal.evapro.toolsapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ApkExporter {

    /**
     * 导出当前应用的 APK 文件到本地存储的 Downloads 文件夹。
     *
     * @param context 上下文
     * @return 导出成功时返回目标文件路径，否则返回 null
     */
    public static String exportApkToDownloads(Context context) {
        // 1. 获取应用 APK 文件路径
        String apkPath = getApkPath(context);
        if (apkPath == null) {
            return null;
        }

        // 2. 设置目标文件路径
        String destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + context.getResources().getString(R.string.app_name) + BuildConfig.impl_info + ".apk";
        File sourceFile = new File(apkPath);
        File destinationFile = new File(destinationPath);

        // 3. 执行文件复制
        try {
            copyFile(sourceFile, destinationFile);
            return destinationPath; // 导出成功，返回文件路径
        } catch (IOException e) {
            e.printStackTrace();
            return null; // 导出失败
        }
    }

    /**
     * 获取当前应用的 APK 文件路径
     *
     * @param context 上下文
     * @return APK 文件路径
     */
    private static String getApkPath(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            return applicationInfo.sourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 复制文件方法
     *
     * @param sourceFile      源文件
     * @param destinationFile 目标文件
     * @throws IOException 文件复制错误时抛出异常
     */
    private static void copyFile(File sourceFile, File destinationFile) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(sourceFile);
             FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }
    }
}
