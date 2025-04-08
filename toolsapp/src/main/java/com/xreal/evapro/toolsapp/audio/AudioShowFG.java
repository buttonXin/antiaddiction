package com.xreal.evapro.toolsapp.audio;

import android.graphics.Color;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.LogControl;

import java.util.List;

public class AudioShowFG extends BaseOLFragment {

    private Button mBtnStart;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("倒计时语音功能-详情");
    }

    final List<AudioBean> audioList = AudioHelper.getInstance().getAudioList();
    private final Runnable runnable = () -> {

    };
    private int currentIndex = -1;

    @Override
    protected boolean isBlackScreen() {
        return true;
    }

    @Override
    protected boolean hasBg() {
        return false;
    }

    @Override
    public void initData() {
        mBtnStart = addButton("开始", 100, v -> {
            currentIndex = 0;
            mBtnStart.setEnabled(false);
            addAllView();
        });
        mBtnStart.setKeepScreenOn(true);
        addButton("暂停", 100, v -> {
            stop();
        });


        addAllView();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
    }

    private void stop() {
        Log.e(TAG, "stop: ");
        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
        mBtnStart.setEnabled(true);
        currentIndex = -1;
        addAllView();
    }

    private void addAllView() {
        clearAllView();
        for (AudioBean audioBean : audioList) {
            addItemAudio(audioBean);
        }
        if (audioList.isEmpty()) {
            toast("没有数据");
            new AudioConfigFG().openFragment(getFragmentManager());
            removeFragment();
        }
    }

    private void clearAllView() {
        for (AudioBean audioBean : audioList) {
            clearHorizontalLlView(audioBean.index);
        }
    }


    private void addItemAudio(AudioBean audioBean) {
        TextView tv = addText("time=" + audioBean.time + "__ index="
                + audioBean.index + "__" + audioBean.audioPath, audioBean.index);

        if (currentIndex == audioBean.index) tv.setTextColor(Color.RED);
        if (currentIndex == audioBean.index) {
            AudioHelper.getInstance().playAudio(audioBean.audioPath);
            handler.postDelayed(() -> {
                currentIndex++;
                addAllView();
            }, (long) audioBean.time * 60 * 1000);
        }
        LogControl.d("currentIndex=", currentIndex, "audioBean.index=", audioBean.index);
        if (currentIndex > 0 && currentIndex >= audioList.size()) {
            stop();
        }
    }
}
