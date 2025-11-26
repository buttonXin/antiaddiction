package com.xreal.evapro.toolsapp.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArraySet;

import com.xreal.evapro.toolsapp.App;
import com.xreal.evapro.toolsapp.base.IResult;
import com.xreal.evapro.toolsapp.util.LogControl;
import com.xreal.evapro.toolsapp.util.SPUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class AudioHelper {

    private static final String TAG = AudioHelper.class.getSimpleName();

    private AudioManager audioManager;
    private int originalVolume = -1;
    private int maxVolume;
    private static final String KEY_AUDIO_LIST = "audio_list";
    // 音频的分隔符
    public static final String KEY_AUDIO_SPLIT = "#__#";
    // 时间的分隔符
    public static final String KEY_TIME_SPLIT = "#_time_#";
    private volatile static AudioHelper sInstance = null;

    private AudioHelper() {
        audioManager = (AudioManager) App.getInstance().getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public static AudioHelper getInstance() {
        if (sInstance == null) {
            synchronized (AudioHelper.class) {
                if (sInstance == null) {
                    sInstance = new AudioHelper();
                }
            }
        }
        return sInstance;
    }

    private MediaRecorder mediaRecorder;
    private String outputPath;

    public boolean startRecording() {
        File outputDir = App.getInstance().getCacheDir();
        outputPath = new File(outputDir, "recording_" + System.currentTimeMillis() + ".mp3").getAbsolutePath();
        return startRecording(outputPath);
    }

    public boolean startRecording(String outputPath) {
        try {
            // 创建临时文件
            this.outputPath = outputPath;
            if (mediaPlayer == null) {
                mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            }
            mediaRecorder.setOutputFile(outputPath);
            LogControl.d(TAG, "startRecording: " + outputPath);

            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            LogControl.d(TAG, "录音初始化失败", e);
            return false;
        }
        return true;
    }

    public String stopRecording(int time) {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                LogControl.d(" mediaRecorder.stop failed. ");
            }

        }
        if (time <= 0) {
            return outputPath;
        }
        final Set<String> stringSet = SPUtils.getInstance().getStringSet(KEY_AUDIO_LIST, new ArraySet<>());
        SPUtils.getInstance().put(KEY_AUDIO_LIST, new ArraySet<>());

        final int size = stringSet.size();
        final String sp_save_audio_str = time + KEY_TIME_SPLIT + size + KEY_AUDIO_SPLIT + outputPath;
        stringSet.add(sp_save_audio_str);
        LogControl.d(TAG, "stopRecording: " + sp_save_audio_str);
        SPUtils.getInstance().put(KEY_AUDIO_LIST, stringSet, false);
        return outputPath;

    }

    public String getOutputPath() {
        return outputPath;
    }

    public void saveAudioVolume(float volume) {
        LogControl.d("volume", volume);
        SPUtils.getInstance().put("volume", volume);
    }

    private MediaPlayer mediaPlayer;

    public boolean playAudio(String filePath) {
        return playAudio(filePath, null);
    }

    public boolean playAudio(String filePath, IResult<Void> iResult) {
        LogControl.d(TAG, "playAudio: " + filePath);

        if (TextUtils.isEmpty(filePath)) {
            return false;
        }

        if (mediaPlayer != null) {
            stopPlaying();
        }

        originalVolume = -1;
        if (filePath.contains(KEY_AUDIO_SPLIT)) {
            filePath = filePath.split(KEY_AUDIO_SPLIT)[1];
            LogControl.d(TAG, "playAudio: " + filePath);

            // 保存当前音量
            originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            final float volume = SPUtils.getInstance().getFloat("volume", 0.7f);
            LogControl.d("volume", volume);
            // 设置音量为最大音量的70%
            int targetVolume = (int) (maxVolume * volume);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0);
        }


        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());

                mediaPlayer.prepare();
                mediaPlayer.setOnCompletionListener(mp -> {
                    // 播放结束后恢复音量
                    if (originalVolume != -1) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
                    }
                    stopPlaying();
                    if (iResult != null) {
                        iResult.onResult(null);
                    }
                });
            }


            mediaPlayer.start();
        } catch (IOException e) {
            LogControl.d("AudioPlayer", "播放失败", e);
            return false;
        }
        return true;
    }

    public boolean playAudio(Context context, Uri uri, IResult<Void> iResult) {
        LogControl.d(TAG, "playAudio: " + uri);

        if (uri == null) {
            return false;
        }

        if (mediaPlayer != null) {
            stopPlaying();
        }

        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();

                mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
                mediaPlayer.setOnCompletionListener(mp -> {
                    stopPlaying();
                    if (iResult != null) {
                        iResult.onResult(null);
                    }
                });
            }
            mediaPlayer.setDataSource(context, uri);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            LogControl.d("AudioPlayer", "播放失败", e);
            return false;
        }
        return true;
    }

    public void stopPlaying() {
        LogControl.d();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void clearDeleteAudioFile() {
        for (AudioBean audioBean : getAudioList()) {
            final File file = new File(audioBean.audioPath);
            if (!file.exists()) {
                continue;
            }
            file.delete();

        }
        SPUtils.getInstance().put(KEY_AUDIO_LIST, new ArraySet<>());
    }

    public List<AudioBean> getAudioList() {
        final Set<String> stringSet = SPUtils.getInstance().getStringSet(KEY_AUDIO_LIST, new ArraySet<>());
        final List<AudioBean> audioBeans = new ArrayList<>();

        for (String item : stringSet) {
            audioBeans.add(AudioBean.split(item));
        }

        audioBeans.sort(Comparator.comparingInt(bean -> bean.index));
        return audioBeans;
    }

}
