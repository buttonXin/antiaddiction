package com.xreal.evapro.toolsapp.camrea;

import android.graphics.drawable.BitmapDrawable;
import android.util.Size;
import android.widget.ImageView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;

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
            final BitmapDrawable bitmapDrawable = new BitmapDrawable(mActivity.getResources(), file.getPath());
            ImageView view = addImage(bitmapDrawable, new Size(450, 800));
            view.setOnClickListener(v -> new CacheImgSingleFG().openFragment(getFragmentManager()).setBaseParams(file.getPath()));

        }
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
