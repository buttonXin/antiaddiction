package com.xreal.evapro.toolsapp.video;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.xreal.evapro.toolsapp.R;
import com.xreal.evapro.toolsapp.base.BaseOLActivity;
import com.xreal.evapro.toolsapp.util.DensityUtil;
import com.xreal.evapro.toolsapp.util.LogControl;

public class FullVideoAct extends BaseOLActivity {

    private Button mFullBtn;
    private String mM3u8Url;

    @Override
    protected boolean hasFullScreen() {
        return true;
    }

    private SimpleExoPlayer player;
    private PlayerView playerView;
    private static final String M3U8_URL = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";

    @Override
    protected void addBg(int color) {
        super.addBg(Color.BLACK);
    }

    @Override
    public void initData() {

//        addButton("全屏", v -> enterFullscreen());
//        addButton("退出全屏", v -> exitFullscreen());

        playerView = new PlayerView(this);
        playerView.setUseController(true);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        addFullscreenView(playerView).post(() -> {
            initPlayer();
            new VideoDoubleTapHelper(playerView, player);
        });

        // 添加点击监听器显示按钮
        playerView.setOnClickListener(v -> {
            showFullscreenButton();
        });
        playerView.setKeepScreenOn(true);

        addBottom();


    }

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideButtonRunnable = new Runnable() {
        @Override
        public void run() {
            if (mFullBtn != null) {
                mFullBtn.setVisibility(View.GONE);
            }
        }
    };

    /**
     * 显示全屏按钮并设置2秒后自动隐藏
     */
    private void showFullscreenButton() {
        if (mFullBtn != null) {
            mFullBtn.setVisibility(View.VISIBLE);
            // 移除之前的隐藏任务，防止重复执行
            handler.removeCallbacks(hideButtonRunnable);
            // 2秒后自动隐藏按钮
            handler.postDelayed(hideButtonRunnable, 2000);
        }
    }

    private void addBottom() {

        mFullBtn = new Button(this);
        mFullBtn.setText("[ 全屏 ]");
        mFullBtn.setAllCaps(false);
        mFullBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mFullBtn.setAllCaps(false);
        mFullBtn.setBackgroundResource(R.drawable.button_selector);
        int padding = DensityUtil.dip2px(10);
        mFullBtn.setPadding(padding, padding, padding, padding);
        mFullBtn.setOnClickListener(v -> {
            int currentOrientation = this.getResources().getConfiguration().orientation;

            if (currentOrientation == android.content.res.Configuration.ORIENTATION_PORTRAIT) {
                // 当前是竖屏，切换到横屏
                enterFullscreen();
            } else {
                // 当前是横屏，切换到竖屏
                exitFullscreen();
            }

        });
        final FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.rightMargin = getBottomMargin();
        btnParams.bottomMargin = 30;
        btnParams.gravity = Gravity.END | Gravity.BOTTOM;
        mFullBtn.setLayoutParams(btnParams);
        addFullscreenView(mFullBtn);

    }

    private String getM3u8Url() {

        final String url = getIntent().getStringExtra("url");
        LogControl.d(" getM3u8Url", "url: " + url);
        if (!TextUtils.isEmpty(url)) {
            return url;
        }

        final String clipContent = getClipContent();
        LogControl.d(" getM3u8Url", "clipContent: " + clipContent);
        if (TextUtils.isEmpty(clipContent)) {
            return M3U8_URL;
        }
        return clipContent;
    }

    /**
     * 获取剪切板的内容
     */
    public String getClipContent() {
        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {
                CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();
                String addedTextString = String.valueOf(addedText);
                if (!TextUtils.isEmpty(addedTextString)) {
                    return addedTextString;
                }
            }
        }
        return "";
    }

    private void initPlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
        // 清理Handler回调，防止内存泄漏
        if (handler != null) {
            handler.removeCallbacks(hideButtonRunnable);
        }

        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        DefaultHttpDataSource.Factory factory = new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true).setUserAgent("Mozilla/5.0");

        mM3u8Url = getM3u8Url();
        MediaItem mediaItem = MediaItem.fromUri(mM3u8Url);

        MediaSource mediaSource = new DefaultMediaSourceFactory(factory).createMediaSource(mediaItem);

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
    }


    private void enterFullscreen() {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void exitFullscreen() {
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        this.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(mM3u8Url)) {
            return;
        }
        // Denying clipboard access to com.oldhigh.toolsapp, application is not in focus nor is it a system service for user 0
        // 延时执行
        handler.postDelayed(() -> {
            final String clipContent = getClipContent();
            LogControl.d( "clipContent: " + clipContent);
            if (!mM3u8Url.equals(clipContent)) {
                initPlayer();
            }
        },1000);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        // 清理Handler回调，防止内存泄漏
        if (handler != null) {
            handler.removeCallbacks(hideButtonRunnable);
        }
    }


}
