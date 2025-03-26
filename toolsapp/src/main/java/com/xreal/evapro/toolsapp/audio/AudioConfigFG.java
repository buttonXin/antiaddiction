package com.xreal.evapro.toolsapp.audio;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;

import java.util.List;

public class AudioConfigFG extends BaseOLFragment {
    private static final String TAG = AudioConfigFG.class.getSimpleName();
    private TextView mTVStartTime;
    private Button mBtnRecord;
    private int noteTime = 1;
    private TextView mTVDelayTime;
    private TextView mTVVolume;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("倒计时语音功能");
    }


    @Override
    public void initData() {

        addButton("清空所有录音", v -> {
            AudioHelper.getInstance().clearDeleteAudioFile();
        });
        delayTimeView();
        configAudioVolume();
        audioRecord();

        addButton("所以音频内容", v -> {
            final List<AudioBean> audioList = AudioHelper.getInstance().getAudioList();
            for (AudioBean audioBean : audioList) {
                addText("time=" + audioBean.time + "__ index="
                        + audioBean.index + "__" + audioBean.audioPath, audioBean.index);
            }

        });
    }

    private void audioRecord() {
        mBtnRecord = addButton("录制音频", 20, v -> {
            AudioHelper.getInstance().startRecording();
            startTimer();
            mTVStartTime.setVisibility(View.VISIBLE);
            mBtnRecord.setEnabled(false);
        });
        addButton("停止音频", 20, v -> {
            AudioHelper.getInstance().stopRecording(noteTime);
            mBtnRecord.setEnabled(true);
            stopTimer();
            noteTime = 1;
            mTVDelayTime.setText(String.format("倒计时:%d 分", noteTime));
        });
        mTVStartTime = addText("录制时间:", 20);
    }

    private void delayTimeView() {
        mTVDelayTime = addText(String.format("倒计时:%d 分", noteTime), 30);
        addButton("+", 30, v -> {
            noteTime++;
            mTVDelayTime.setText(String.format("倒计时:%d 分", noteTime));
        });
        addButton("-", 30, v -> {
            noteTime--;
            if (noteTime <= 1) {
                noteTime = 1;
            }
            mTVDelayTime.setText(String.format("倒计时:%d 分", noteTime));
        });
    }

    private float audioVolume = 0.7f;

    private void configAudioVolume() {
        mTVVolume = addText("音量百分百: " + (audioVolume * 100) + "%", 40);
        addButton("+", 40, v -> {
            audioVolume += 0.1F;
            if (audioVolume >= 1) {
                audioVolume = 1;
            }
            mTVVolume.setText("音量百分百: " + (audioVolume * 100) + "%");
            AudioHelper.getInstance().saveAudioVolume(audioVolume);
        });
        addButton("-", 40, v -> {
            audioVolume -= 0.1f;
            if (audioVolume <= 0.1f) {
                audioVolume = 0.1f;
            }
            AudioHelper.getInstance().saveAudioVolume(audioVolume);
            mTVVolume.setText("音量百分百: " + (audioVolume * 100) + "%");
        });
    }


    private Runnable runnable;
    private int seconds = 0;

    private void startTimer() {
        seconds = 0;
        runnable = new Runnable() {
            @Override
            public void run() {
                seconds++;
                mTVStartTime.setText(String.format("录制时间:%d", seconds));
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);
    }

    private void stopTimer() {
        handler.removeCallbacks(runnable);
    }
}
