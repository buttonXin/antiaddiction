package com.xreal.evapro.toolsapp.audio;

import com.xreal.evapro.toolsapp.util.LogControl;

public class AudioBean {
    private static final String TAG = AudioBean.class.getSimpleName();
    public int index;
    public int time;
    public String audioPath;


    public AudioBean(int index, int time, String audioPath) {
        this.index = index;
        this.time = time;
        this.audioPath = audioPath;
    }


    /**
     * @param spContent 2#_time_#1#__#/data/user/0/com.xreal.evapro.toolsapp/cache/recording_1742977924276.mp3
     *                  2#_time_#   代表2分钟
     *                  1#__#       代表位置
     *                  1#__#后面的    代表路径
     */
    public static AudioBean split(String spContent) {
        final String time = spContent.split(AudioHelper.KEY_TIME_SPLIT)[0];
        final String index = spContent.split(AudioHelper.KEY_TIME_SPLIT)[1].split(AudioHelper.KEY_AUDIO_SPLIT)[0];
        final String audioPath = spContent.split(AudioHelper.KEY_TIME_SPLIT)[1].split(AudioHelper.KEY_AUDIO_SPLIT)[1];

        final AudioBean audioBean = new AudioBean(Integer.parseInt(index), Integer.parseInt(time), audioPath);

        LogControl.d(TAG, "split: " + audioBean);
        return audioBean;
    }

    @Override
    public String toString() {
        return "AudioBean{" +
                "index=" + index +
                ", time=" + time +
                ", audioPath='" + audioPath + '\'' +
                '}';
    }
}
