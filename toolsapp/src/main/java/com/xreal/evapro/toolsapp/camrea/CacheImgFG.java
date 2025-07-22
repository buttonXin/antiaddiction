package com.xreal.evapro.toolsapp.camrea;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Size;
import android.widget.ImageView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class CacheImgFG extends BaseOLFragment {
    @Override
    public void initData() {

        addButton("清理所有", v -> clearFile());

        File mediaStorageDir = new File(mActivity.getExternalCacheDir(), "my_camera_file");
        if (!mediaStorageDir.exists()) {
            toast("not cache");
            return;
        }
        final String[] list = mediaStorageDir.list();
        if (list == null) {
            toast("not cache");
            return;
        }


        for (String name : list) {
            final File file = new File(mediaStorageDir, name);
            final BitmapDrawable bitmapDrawable = getBitmapDrawable(file);

            ImageView view = addImage(bitmapDrawable, new Size(450, 800));
            view.setOnClickListener(v -> new CacheImgSingleFG().openFragment(getFragmentManager()).setBaseParams(file.getPath()));

        }
    }

    private BitmapDrawable getBitmapDrawable(File file) {
        // Step 1: 从文件解码 Bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Step 2: 设置缩放比例（可选，根据目标尺寸计算 inSampleSize）
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(options, 450, 800);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Step 3: 压缩 Bitmap 质量
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream); // 压缩质量为 70%
        byte[] byteArray = stream.toByteArray();
        Bitmap compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        return new BitmapDrawable(getResources(), compressedBitmap);
    }

    // 辅助方法：计算 inSampleSize
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfWidth = width / 2;
            final int halfHeight = height / 2;
            while ((halfWidth / inSampleSize) >= reqWidth
                    && (halfHeight / inSampleSize) >= reqHeight) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private void clearFile() {
        File mediaStorageDir = new File(mActivity.getExternalCacheDir(), "my_camera_file");
        if (mediaStorageDir.exists()) {
            for (File file : mediaStorageDir.listFiles()) {
                file.delete();
            }
        }
    }


}
