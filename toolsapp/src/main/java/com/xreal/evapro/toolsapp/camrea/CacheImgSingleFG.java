package com.xreal.evapro.toolsapp.camrea;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.Size;
import android.widget.ImageView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.LogControl;

import java.io.File;

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
            sharedImg(new File( content));
            return false;
        });
    }
    private void sharedImg(File file) {
        Uri uri = MyFileProvider.getUriForFile(file);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri); // 添加图片 Uri
        shareIntent.setType("image/*"); // 设置分享类型为图片
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // 授予临时读取权限
        mActivity.startActivity(Intent.createChooser(shareIntent, "分享图片到")); // 弹出选择框
    }

}
