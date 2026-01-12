package com.xreal.evapro.toolsapp.camrea;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.LogControl;
import com.xreal.evapro.toolsapp.util.SPUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

public class CacheImgSingleFG extends BaseOLFragment {

    @Override
    public void initData() {

        final String content = getContent();

        LogControl.d("content:" + content);
        ImageView imageView = new ImageView(mActivity);
        final BitmapDrawable bitmapDrawable = new BitmapDrawable(
                mActivity.getResources(),
                compressImage(content));
        imageView.setImageDrawable(bitmapDrawable);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addFullscreenView(imageView);
        imageView.setOnClickListener(v -> closeFragment());
        imageView.setOnLongClickListener(v -> {
            saveImage(new File(content));
            return false;
        });
    }


    private String compressImage(String imagePath) {
        try {
            // 获取原始图片的尺寸
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;  // 只获取边界信息
            BitmapFactory.decodeFile(imagePath, options);

            // 计算压缩比例
            int width = options.outWidth;
            int height = options.outHeight;
            int maxWidth = 1080;  // 最大宽度
            int maxHeight = 1920; // 最大高度

            // 计算缩放比例
            int scale = 1;
            while (width > maxWidth || height > maxHeight) {
                width /= 2;
                height /= 2;
                scale *= 2;
            }

            // 实际解码并压缩图片
            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;  // 设置采样率

            Bitmap compressedBitmap = BitmapFactory.decodeFile(imagePath, options);

            // 将压缩后的图片保存到缓存目录
            File cacheDir = mActivity.getExternalCacheDir();
            if (cacheDir == null) {
                cacheDir = mActivity.getCacheDir();
            }
            String fileName = "compressed_" + new File(imagePath).getName();
            File compressedFile = new File(cacheDir, fileName);

            // 将压缩后的图片保存到文件
            FileOutputStream fos = new FileOutputStream(compressedFile);
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos); // 80% 质量
            fos.close();

            return compressedFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            // 如果压缩失败，返回原图路径
            return imagePath;
        }
    }

    private void saveImage(File pictureFile) {
        try {

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, pictureFile.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);


            ContentResolver contentResolver = mActivity.getContentResolver();
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
                sharedImg(uri);
//                pictureFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            toast("failed");
        }
    }

    private void sharedImg(Uri uri) {
//        Uri uri = MyFileProvider.getUriForFile(file);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri); // 添加图片 Uri
        shareIntent.setType("image/*"); // 设置分享类型为图片
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 授予临时读取权限
        mActivity.startActivity(Intent.createChooser(shareIntent, "分享图片到")); // 弹出选择框

        Set<String> stringSet = SPUtils.getInstance().getStringSet("share_img");
        final HashSet<String> endSets = new HashSet<>();
        endSets.add(uri.toString());
        if (stringSet != null) {
            endSets.addAll(stringSet);
        }
        SPUtils.getInstance().put("share_img", endSets);
    }
}
