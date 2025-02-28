package com.xreal.evapro.toolsapp;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.xreal.evapro.toolsapp.camrea.CameraPreviewFG;
import com.xreal.evapro.toolsapp.local_server.LocalServerFG;

public class MainActivity extends BaseOLActivity {

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar(getString(R.string.app_name));
    }

    @Override
    public void initData() {
        addButton("本地服务器功能-全文件  -> ", v -> new LocalServerFG().openFragment(getFragmentManager()));
        // 仅下载的文件  好多无法显示
//        addButton("本地服务器功能-仅下载文件  -> ", v -> new LocalServerFG(true).openFragment(getFragmentManager()));

        addButton("后置摄像头全屏预览  -> ", v -> new CameraPreviewFG(0).openFragment(getFragmentManager()));
        addButton("前置摄像头全屏预览  -> ", v -> new CameraPreviewFG(1).openFragment(getFragmentManager()));


    }
}
