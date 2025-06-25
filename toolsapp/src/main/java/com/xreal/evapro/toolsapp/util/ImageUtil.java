package com.xreal.evapro.toolsapp.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;

public class ImageUtil {


    /**
     * 通过 Uri 加载并压缩图片
     *
     * @param context   上下文
     * @param uri       图片的 Uri
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     * @return 压缩后的 Bitmap
     */
    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri,
                                                    int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            // 获取文件描述符
            ParcelFileDescriptor parcelFileDescriptor =
                    contentResolver.openFileDescriptor(uri, "r");
            if (parcelFileDescriptor == null) return null;
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            // 第一次解析获取图片尺寸
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

            // 计算 inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // 第二次解析加载压缩后的图片
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

            parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 按目标宽高压缩图片并返回 Bitmap
     *
     * @param resources 资源对象
     * @param resId     图片资源 ID
     * @param reqWidth  目标宽度
     * @param reqHeight 目标高度
     * @return 压缩后的 Bitmap
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources resources, int resId,
                                                         int reqWidth, int reqHeight) {
        // 第一次解析将 inJustDecodeBounds 设为 true，获取图片尺寸
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources, resId, options);

        // 计算 inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // 第二次解析将 inJustDecodeBounds 设为 false，返回压缩后的 Bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources, resId, options);
    }

    /**
     * 根据目标宽高计算 inSampleSize 值
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfWidth = width / 2;
            final int halfHeight = height / 2;

            // 计算最大的 inSampleSize，使宽高都小于等于目标值（且是 2 的幂）
            while ((halfWidth / inSampleSize) >= reqWidth
                    && (halfHeight / inSampleSize) >= reqHeight) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    /**
     * 跳转选择相册
     */
    private static void openGallery(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 180 && resultCode == RESULT_OK && data != null) {
//            Uri selectedImageUri = data.getData();
//            // 使用Glide加载图片
//
//            final Bitmap bitmap = ImageUtil.decodeSampledBitmapFromUri(this, selectedImageUri, mImageView.getWidth(), mImageView.getHeight());
//            mImageView.setImageBitmap(bitmap);
////            mImageView.setImageURI(selectedImageUri);
//
//        }
//    }
}
