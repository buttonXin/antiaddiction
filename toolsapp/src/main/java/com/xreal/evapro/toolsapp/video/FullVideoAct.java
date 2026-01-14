package com.xreal.evapro.toolsapp.video;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.xreal.evapro.toolsapp.base.BaseOLActivity;
import com.xreal.evapro.toolsapp.util.LogControl;

public class FullVideoAct extends BaseOLActivity {

    @Override
    protected boolean hasFullScreen() {
        return true;
    }

    private SimpleExoPlayer player;
    private PlayerView playerView;
    private static final String M3U8_URL =
            "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";

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
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        DefaultHttpDataSource.Factory factory =
                new DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                        .setUserAgent("Mozilla/5.0");

        MediaItem mediaItem = MediaItem.fromUri(getM3u8Url());

        MediaSource mediaSource =
                new DefaultMediaSourceFactory(factory)
                        .createMediaSource(mediaItem);

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);
    }


    private void enterFullscreen() {
        this.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void exitFullscreen() {
        this.setRequestedOrientation(
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_VISIBLE);
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
    }


}
