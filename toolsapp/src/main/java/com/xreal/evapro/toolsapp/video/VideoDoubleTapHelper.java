package com.xreal.evapro.toolsapp.video;

import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

public class VideoDoubleTapHelper {

    private final PlayerView playerView;
    private final ExoPlayer player;

    // 双击计时器
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastTapTime = 0;
    private int tapCount = 0;
    private static final long DOUBLE_TAP_INTERVAL = 1500; // 2秒内连续双击

    public VideoDoubleTapHelper(PlayerView playerView, ExoPlayer player) {
        this.playerView = playerView;
        this.player = player;

        initTouchListener();
    }

    private void initTouchListener() {
        final GestureDetector gestureDetector = new GestureDetector(playerView.getContext(),
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        float x = e.getX();
                        float width = playerView.getWidth();

                        long currentTime = System.currentTimeMillis();

                        // 检测是否在2秒内连续双击
                        if (currentTime - lastTapTime <= DOUBLE_TAP_INTERVAL) {
                            tapCount++;
                        } else {
                            tapCount = 1; // 新的双击开始计数
                        }

                        lastTapTime = currentTime;

                        // 计算快进/快退时间，每次加10秒，最多60秒
                        int seekTime = Math.min(tapCount * 10_000, 60_000); // 毫秒

                        if (x > width / 2f) {
                            // 右半边 - 快进
                            long targetPosition = player.getCurrentPosition() + seekTime;
                            targetPosition = Math.min(targetPosition, player.getDuration());
                            player.seekTo(targetPosition);
                        } else {
                            // 左半边 - 快退
                            long targetPosition = player.getCurrentPosition() - seekTime;
                            targetPosition = Math.max(targetPosition, 0);
                            player.seekTo(targetPosition);
                        }

                        // 2秒后重置tapCount
                        handler.removeCallbacks(resetTapRunnable);
                        handler.postDelayed(resetTapRunnable, DOUBLE_TAP_INTERVAL);

                        return true;
                    }
                });

        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, @NonNull MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    private final Runnable resetTapRunnable = new Runnable() {
        @Override
        public void run() {
            tapCount = 0;
        }
    };
}

