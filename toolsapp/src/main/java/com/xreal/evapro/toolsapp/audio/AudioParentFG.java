package com.xreal.evapro.toolsapp.audio;

import android.Manifest;
import android.content.pm.PackageManager;

import com.xreal.evapro.toolsapp.util.LogControl;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;

/**
 * 音频功能
 */
public class AudioParentFG extends BaseOLFragment {

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("音频功能");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogControl.d(TAG, "onActivityResult: " + requestCode);
        if (requestCode == 110) {
            toast("权限已经允许");
        }
    }

    @Override
    public void initData() {
        final boolean audioPermission = mActivity.checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
        LogControl.d(TAG, "initData: audioPermission = " + audioPermission);


        if (!audioPermission) {
            addButton("申请音频权限", v -> requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 110));
        }

        addButton("倒计时语音功能-配置", v -> new AudioConfigFG().openFragment(getFragmentManager()));
        addButton("显示", v -> new AudioShowFG().openFragment(getFragmentManager()));
        addButton("复读机", v -> new AudioRepeaterFG().setBaseParams("复读机").openFragment(getFragmentManager()));
    }
}
