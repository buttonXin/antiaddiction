package com.oldhigh.antiaddiction.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;

import com.oldhigh.antiaddiction.App;
import com.oldhigh.antiaddiction.HomeAct;
import com.oldhigh.antiaddiction.action.OperationAction;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.ven.assists.stepper.StepManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {

    private static final String TAG = FileUtil.class.getSimpleName();
    private static android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

    public static void deleteNonTodayFiles(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            Log.w(TAG, "目录不存在或不是目录: " + directoryPath);
            return;
        }

        // 获取今天的日期字符串
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // 遍历目录中的所有文件
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    // 检查文件名是否包含今天的日期
                    if (!isTodayFile(file.getName(), today)) {
                        // 删除非当天文件
                        boolean deleted = file.delete();
                        if (deleted) {
                            Log.d(TAG, "已删除文件: " + file.getName());
                        } else {
                            Log.w(TAG, "删除文件失败: " + file.getName());
                        }
                    }
                }
            }
        }
    }

    private static boolean isTodayFile(String fileName, String today) {
        // 这里可以根据实际文件命名规则进行调整
        // 假设文件名包含日期信息，如 screenshot_20231201_123456.jpg
        return fileName.contains(today);
    }

    public static File getOutputMediaFile() {
        File mediaStorageDir = new File(App.getInstance().getExternalCacheDir(), "my_camera_file");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // 删除非当天的其他文件
        deleteNonTodayFiles(mediaStorageDir.getPath());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }

    // 保存 Bitmap 到文件
    public static String saveBitmapToFile(Bitmap bitmap) {
        try {
            // 创建文件路径
            final File outputMediaFile = getOutputMediaFile();

            // 保存图片
            FileOutputStream fos = new FileOutputStream(outputMediaFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            return outputMediaFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Uri saveImageToUri(File pictureFile) {
        try {

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, pictureFile.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);


            ContentResolver contentResolver = App.getInstance().getContentResolver();
            Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                // 直接从文件复制到 MediaStore
                try (FileInputStream fis = new FileInputStream(pictureFile);
                     OutputStream output = contentResolver.openOutputStream(uri)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }
                return uri;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 分享图片到微信
    public static void shareImageToWeChat(String imagePath) {
        handler.post(() -> {


            final Uri uri = saveImageToUri(new File(imagePath));
            if (uri == null) {
                Log.e(TAG, "保存图片失败");
                return;
            }
            Log.d(TAG, "图片已保存到: " + uri);

            SPUtils.getInstance().put(SpKey.screenshot_uri, uri.toString());
            handler.postDelayed(() -> {
                StepManager.INSTANCE.execute(OperationAction.class, 1, 100, "微信图片", true);
            }, 2000);

//            Intent shareIntent = new Intent();
//            shareIntent.setAction(Intent.ACTION_SEND);
//            shareIntent.putExtra(Intent.EXTRA_STREAM, uri); // 添加图片 Uri
//            shareIntent.setType("image/*"); // 设置分享类型为图片
//            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 授予临时读取权限
//
//
//            try {
//                App.getInstance().startActivity(Intent.createChooser(shareIntent, "分享截图到微信"));
//
//
//            } catch (Exception e) {
//                // 如果没有安装微信，提示用户
//            }

        });
    }
}
