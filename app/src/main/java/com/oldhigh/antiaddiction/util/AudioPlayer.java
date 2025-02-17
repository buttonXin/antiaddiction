package com.oldhigh.antiaddiction.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class AudioPlayer {
    private MediaPlayer mediaPlayer;

    private static final String TAG = AudioPlayer.class.getSimpleName();
     private volatile static AudioPlayer sInstance = null;

         private AudioPlayer() {
         }

         public static AudioPlayer getInstance() {
             if (sInstance == null) {
                 synchronized (AudioPlayer.class) {
                     if (sInstance == null) {
                         sInstance = new AudioPlayer();
                     }
                 }
             }
             return sInstance;
         }
    public void playFromAssets(Context context, String fileName) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();

        try {
            AssetFileDescriptor afd = context.getAssets().openFd(fileName);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();

            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                Log.d("AudioPlayer", "播放完成");
                mp.release();
            });

        } catch (IOException e) {
            Log.e("AudioPlayer", "播放失败: " + e.getMessage());
        }
    }

    public void stop() {
             Log.e(TAG, "stop: " );
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
