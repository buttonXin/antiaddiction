package com.xreal.evapro.toolsapp.camrea;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.LogControl;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class CacheImgSingleFG extends BaseOLFragment {

    @Override
    public void initData() {

        final String content = getContent();

        LogControl.d("content:" + content);
        ImageView imageView = new ImageView(mActivity);
        final BitmapDrawable bitmapDrawable = new BitmapDrawable(
                mActivity.getResources(),
                content);
        imageView.setImageDrawable(bitmapDrawable);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        addFullscreenView(imageView);
        imageView.setOnClickListener(v -> closeFragment());
        imageView.setOnLongClickListener(v -> {
            saveImage(new File(content));
            return false;
        });
    }

    private void saveImage(File pictureFile) {
        try {

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, pictureFile.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            values.put(MediaStore.Images.Media.DATA, pictureFile.getAbsolutePath());

            ContentResolver contentResolver = mActivity.getContentResolver();
            Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            byte[] buffer = new byte[(int) pictureFile.length()];
            //noinspection ResultOfMethodCallIgnored
            java.io.FileInputStream fis = new FileInputStream(pictureFile);
            fis.read(buffer);
            fis.close();
            if (uri != null) {
                OutputStream output = contentResolver.openOutputStream(uri);
                output.write(buffer);
                sharedImg(uri);
                pictureFile.delete();
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
    }
}
