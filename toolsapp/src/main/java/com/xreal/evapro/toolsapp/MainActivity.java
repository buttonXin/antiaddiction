package com.xreal.evapro.toolsapp;

import com.xreal.evapro.toolsapp.audio.AudioParentFG;
import com.xreal.evapro.toolsapp.base.BaseOLActivity;
import com.xreal.evapro.toolsapp.camrea.CameraPreviewFG;
import com.xreal.evapro.toolsapp.local_server.LocalServerFG;

public class MainActivity extends BaseOLActivity {

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar(getString(R.string.app_name));
    }

    @Override
    public void initData() {
        addButton("本地服务器功能-全文件", 1, v -> new LocalServerFG().openFragment(getFragmentManager()));
        // 仅下载的文件  好多无法显示
        addButton("仅下载文件", 1, v -> new LocalServerFG(true).openFragment(getFragmentManager()));

        addButton("后置摄像头", 2, v -> new CameraPreviewFG(0).openFragment(getFragmentManager()));
        addButton("前置摄像头", 2, v -> new CameraPreviewFG(1).openFragment(getFragmentManager()));
        addButton("语音功能", v -> new AudioParentFG().openFragment(getFragmentManager()));


    }
}
