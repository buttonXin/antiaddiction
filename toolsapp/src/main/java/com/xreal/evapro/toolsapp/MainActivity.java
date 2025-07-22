package com.xreal.evapro.toolsapp;

import static com.xreal.evapro.toolsapp.HomeConfigFg.AUDIO_PAGE;
import static com.xreal.evapro.toolsapp.HomeConfigFg.CAMERA_PAGE;
import static com.xreal.evapro.toolsapp.HomeConfigFg.LOCAL_SERVER_PAGE;
import static com.xreal.evapro.toolsapp.HomeConfigFg.MAGNIFIER_PAGE;

import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.xreal.evapro.toolsapp.audio.AudioParentFG;
import com.xreal.evapro.toolsapp.base.BaseOLActivity;
import com.xreal.evapro.toolsapp.camrea.CameraPreviewFG;
import com.xreal.evapro.toolsapp.local_server.LocalServerFG;
import com.xreal.evapro.toolsapp.magnifier.MagnifierShowFG;
import com.xreal.evapro.toolsapp.note.NoteAct;
import com.xreal.evapro.toolsapp.util.DensityUtil;
import com.xreal.evapro.toolsapp.util.LogControl;
import com.xreal.evapro.toolsapp.util.SPUtils;

public class MainActivity extends BaseOLActivity {

    private Button btnLocalServer;
    private Button btnLocalServer2;
    private Button btnCamera;
    private Button btnCamera2;
    private Button btnAudio;
    private Button btnMagnifier;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar(getString(R.string.app_name));
    }

    @Override
    public void initData() {
        addBottom();

        LogControl.d(" CacheImgSingleFG initData");
        btnLocalServer = addButton("本地服务器功能-全文件", 1, v -> new LocalServerFG().openFragment(getFragmentManager()));
        // 仅下载的文件  好多无法显示
        btnLocalServer2 = addButton("仅下载文件", 1, v -> new LocalServerFG().setBaseParams("true").openFragment(getFragmentManager()));
        btnCamera = addButton("后置摄像头", 2, v -> new CameraPreviewFG().setBaseParams("0").openFragment(getFragmentManager()));
        btnCamera2 = addButton("前置摄像头", 2, v -> new CameraPreviewFG().setBaseParams("1").openFragment(getFragmentManager()));
        btnAudio = addButton("语音功能", v -> new AudioParentFG().openFragment(getFragmentManager()));
        btnMagnifier = addButton("放大镜", v -> new MagnifierShowFG().openFragment(getFragmentManager()));

        showHide();

        addButton("打开记录", v -> startAct(NoteAct.class));
    }

    private void showHide() {
        btnLocalServer.setVisibility(SPUtils.getInstance().getBoolean(LOCAL_SERVER_PAGE, true) ? Button.VISIBLE : Button.GONE);
        btnLocalServer2.setVisibility(SPUtils.getInstance().getBoolean(LOCAL_SERVER_PAGE, true) ? Button.VISIBLE : Button.GONE);

        btnCamera.setVisibility(SPUtils.getInstance().getBoolean(CAMERA_PAGE, true) ? Button.VISIBLE : Button.GONE);
        btnCamera2.setVisibility(SPUtils.getInstance().getBoolean(CAMERA_PAGE, true) ? Button.VISIBLE : Button.GONE);

        btnAudio.setVisibility(SPUtils.getInstance().getBoolean(AUDIO_PAGE, true) ? Button.VISIBLE : Button.GONE);

        btnMagnifier.setVisibility(SPUtils.getInstance().getBoolean(MAGNIFIER_PAGE, true) ? Button.VISIBLE : Button.GONE);
    }

    private void addBottom() {

        Button btn = new Button(this);
        btn.setText("首页配置");
        btn.setAllCaps(false);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        btn.setAllCaps(false);
        btn.setBackgroundResource(R.drawable.button_selector);
        int padding = DensityUtil.dip2px(10);
        btn.setPadding(padding, padding, padding, padding);
        btn.setOnClickListener(v -> {
            new HomeConfigFg().openFragment(getFragmentManager()).setOnDestroyListener(this::showHide);
        });
        final FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.rightMargin = getBottomMargin();
        btnParams.bottomMargin = 30;
        btnParams.gravity = Gravity.END | Gravity.BOTTOM;
        btn.setLayoutParams(btnParams);
        addFullscreenView(btn);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LogControl.d(TAG, "onPause: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogControl.d(TAG, "onResume: ");
    }

}
