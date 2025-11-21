package com.xreal.evapro.toolsapp.audio;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.TextView;

import com.xreal.evapro.toolsapp.MainActivity;
import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.base.IResult;
import com.xreal.evapro.toolsapp.util.LogControl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocalAudioLoopFG extends BaseOLFragment {


    private Map<Uri, String> mAudios = new HashMap<>();
    private long totalTime = 20 * 60 * 1000;// 默认20分钟结束
    private int index = 0;
    private Button mBtn1, mBtn2, mBtn3, mBtn4;
    private TextView mTVTime, mTVStatus, mTextView;

    protected void addTitleBar(String text) {
        super.addTitleBar(getContent());
    }


    @Override
    public void initData() {

        mBtn1 = addButton("添加音频", v -> {
            // 打开文件管理, 选择音频文件

            MainActivity.setOnActivityResult(666, intent -> {
                Uri selectedAudioUri = intent.getData();

                if (selectedAudioUri == null) {
                    toast("未选择音频文件");
                    return;
                }
                // 处理选中的音频文件
                LogControl.d(TAG, "onActivityResult: " + selectedAudioUri, "fileName: " + getAudioFileName(selectedAudioUri));
                mAudios.put(selectedAudioUri, getAudioFileName(selectedAudioUri));
                StringBuilder allFileName = new StringBuilder();
                allFileName.append("已选择音频文件：\n\n");
                for (String value : mAudios.values()) {
                    allFileName.append(value).append("\n");
                }
                mTextView.setText(allFileName.toString());
            });
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            mActivity.startActivityForResult(Intent.createChooser(intent, "选择音频文件"), 666);

        });
        mBtn2 = addButton("定时-默认20分钟", 2, v -> {
            // 打开系统的时间选择器
            // 创建并显示时间选择器对话框
            TimePickerDialog timePickerDialog = new TimePickerDialog(mActivity,
                    (view, hourOfDay, minute) -> {
                        // 将小时和分钟转换为总秒数
                        int totalSeconds = hourOfDay * 3600 + minute * 60;

                        if (totalSeconds <= 0) {
                            totalTime = 20 * 60 * 1000;
                        } else {
                            totalTime = totalSeconds * 1000L;
                        }
                        LogControl.d(TAG, "onTimeSet: " + hourOfDay + ":" + minute);
                        mTVTime.setText(formatTimeDisplay(totalTime));
                    },
                    // 初始小时和分钟 - 从保存的时间计算
                    (int) (totalTime / 1000 / 60 / 60),
                    (int) ((totalTime / 1000 / 60) % 60),
                    true);
            timePickerDialog.show();
        });
        mTVTime = addText(formatTimeDisplay(totalTime), 2);

        mBtn3 = addButton("开始", 100, v -> {
            if (mAudios.isEmpty()) {
                toast("请先添加音频文件");
                return;
            }
            handler.postDelayed(() -> {
                AudioHelper.getInstance().stopPlaying();
                mTVStatus.setText("已结束");
                mBtn3.setEnabled(true);
            }, totalTime);

            playAudio();
            mTVStatus.setText("正在播放...");
        });
        mBtn4 = addButton("结束", 100, v -> {
            AudioHelper.getInstance().stopPlaying();
            mBtn3.setEnabled(true);

            handler.removeCallbacksAndMessages(null);
            mTVStatus.setText("已结束");
        });

        mTVStatus = addText("");
        addLine();
        mTextView = addText("");
    }

    // 修改时间显示方法
    private String formatTimeDisplay(long totalTimeMs) {
        long totalMinutes = totalTimeMs / 1000 / 60;
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        if (hours > 0) {
            return "定时：" + hours + "小时" + minutes + "分钟";
        } else {
            return "定时：" + minutes + "分钟";
        }
    }

    /**
     * 循环播放音频, 等待倒计时结束
     */
    private void playAudio() {
        mBtn3.setEnabled(false);
        final ArrayList<Uri> list = new ArrayList<>(mAudios.keySet());
        final Uri audioUri = list.get(index);
        AudioHelper.getInstance().playAudio(mActivity, audioUri, mResult);
    }

    private final IResult<Void> mResult = unused -> {
        index++;
        if (index >= mAudios.size()) {
            index = 0;
        }
        playAudio();
    };

    private String getAudioFileName(Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = mActivity.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = new File(uri.getPath()).getName();
        }
        return fileName;
    }

}
