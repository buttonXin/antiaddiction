package com.xreal.evapro.toolsapp.audio;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.widget.Button;

import com.xreal.evapro.toolsapp.App;
import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.LogControl;

import java.io.File;

/**
 * 复读机功能
 */
public class AudioRepeaterFG extends BaseOLFragment {

    private Button mBtnStart;
    private Button mBtnTouch;
    private long startTime;
    private final long shortTime = 1500;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar(getContent());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void initData() {
        File outputDir = App.getInstance().getCacheDir();
        final String absolutePath = new File(outputDir, "repeater_temp.mp3").getAbsolutePath();

        mBtnStart = addButton("开始录音", 1, v -> {
            AudioHelper.getInstance().startRecording(absolutePath);
            mBtnStart.setEnabled(false);
        });
        addButton("停止录音", 1, v -> {
            AudioHelper.getInstance().stopRecording(0);
            mBtnStart.setEnabled(true);
        });

        addLine();
        addText("");
        addLine();
        // 按下时,开始录音, 抬起时停止录音
        mBtnTouch = addButton("长按录音", null);
        int padding = 40;
        mBtnTouch.setPadding(padding, padding, padding, padding);
        mBtnTouch.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startTime = System.currentTimeMillis();
                    AudioHelper.getInstance().startRecording(absolutePath);
                    break;
                case MotionEvent.ACTION_UP:
                    if (System.currentTimeMillis() - startTime < shortTime) {
                        LogControl.d(" 录音时间过短");
                        toast("录音时间过短(需大于1.5s)");
                        AudioHelper.getInstance().stopRecording(0);
                        break;
                    }
                    AudioHelper.getInstance().stopRecording(0);
                    AudioHelper.getInstance().playAudio(AudioHelper.getInstance().getOutputPath());
                    break;
            }
            return true;
        });
        addLine();
        addText("");
        addLine();
        addButton("播放录音", v -> {
            AudioHelper.getInstance().playAudio(AudioHelper.getInstance().getOutputPath());
        }).setPadding(padding, padding, padding, padding);
    }
}
