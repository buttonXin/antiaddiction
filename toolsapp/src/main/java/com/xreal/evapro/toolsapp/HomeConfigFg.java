package com.xreal.evapro.toolsapp;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.SPUtils;

public class HomeConfigFg extends BaseOLFragment {
    public static final String LOCAL_SERVER_PAGE = "LOCAL_SERVER_PAGE";
    public static final String CAMERA_PAGE = "CAMERA_PAGE";
    public static final String AUDIO_PAGE = "AUDIO_PAGE";
    public static final String MAGNIFIER_PAGE = "MAGNIFIER_PAGE";


    @Override
    public void initData() {

        addText("-----首页功能是否显示-----");

        addSwitch("本地服务器-功能", SPUtils.getInstance().getBoolean(LOCAL_SERVER_PAGE, true),
                (buttonView, isChecked) -> SPUtils.getInstance().put(LOCAL_SERVER_PAGE, isChecked));

        addSwitch("摄像头-功能", SPUtils.getInstance().getBoolean(CAMERA_PAGE, true),
                (buttonView, isChecked) -> SPUtils.getInstance().put(CAMERA_PAGE, isChecked));

        addSwitch("语音-功能", SPUtils.getInstance().getBoolean(AUDIO_PAGE, true),
                (buttonView, isChecked) -> SPUtils.getInstance().put(AUDIO_PAGE, isChecked));

        addSwitch("放大镜-功能", SPUtils.getInstance().getBoolean(MAGNIFIER_PAGE, true),
                (buttonView, isChecked) -> SPUtils.getInstance().put(MAGNIFIER_PAGE, isChecked));
    }
}
