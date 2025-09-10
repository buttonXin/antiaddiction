package com.oldhigh.antiaddiction.util;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oldhigh.antiaddiction.bean.EventClick;
import com.oldhigh.antiaddiction.feature.SpKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 开启悬浮窗后,有2个按钮,一个是开始, 一个是保存.
 * 开始时,点击界面的某一个点后, 显示当前点的坐标.
 * 点击保存, 保存当前点的坐标.然后关闭悬浮窗.
 */
public class FloatPointHelper {
    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private FrameLayout floatView;
    private Button startButton, saveButton, endButton;
    private TextView coordinateText;
    private boolean isRecording = false;
    private Point currentPoint = null;
    private View redDotView; // 添加红点视图
    private EditText mEditText,mEditTextTime;
    private String mContent;

    public FloatPointHelper(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initLayoutParams();
        initFloatView();
        initRedDotView(); // 初始化红点视图
    }

    private void initLayoutParams() {
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.x = 0;
        layoutParams.y = 100;
    }

    private boolean isStartLocation = false;

    private void initFloatView() {
        floatView = new FrameLayout(context);
//        floatView.setOrientation(LinearLayout.VERTICAL);
        floatView.setBackgroundColor(0x80000000); // 半透明黑色背景

        // 坐标显示文本
        coordinateText = new TextView(context);
        coordinateText.setText("等待开始...");
        coordinateText.setTextColor(0xFFFFFFFF); // 白色文字
        coordinateText.setPadding(20, 20, 20, 20);

        // 按钮容器
        LinearLayout buttonLayout = new LinearLayout(context);
        buttonLayout.setOrientation(LinearLayout.VERTICAL);

        // 开始按钮
        startButton = new Button(context);
        startButton.setText("开始");
        startButton.setOnClickListener(v -> toggleRecording());

        saveButton = new Button(context);
        saveButton.setText("保存");
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(v -> saveCoordinate());

        // 保存按钮

//        mEditText = new EditText(context);
//        mEditText.setHint("操作名称");
//        mEditTextTime = new EditText(context);
//        mEditTextTime.setHint("等待时间");
//        // 只能输入数字
//        mEditTextTime.setInputType(InputType.TYPE_CLASS_NUMBER);

        // 保存按钮
        endButton = new Button(context);
        endButton.setText("结束");
        endButton.setOnClickListener(v -> dismiss());

        Button switchLocationButton = new Button(context);
        switchLocationButton.setText("换位");
        switchLocationButton.setOnClickListener(v -> {
            FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            if (isStartLocation) {
                buttonParams.gravity = Gravity.START | Gravity.TOP;
            } else {
                buttonParams.gravity = Gravity.START | Gravity.BOTTOM;
            }
            isStartLocation = !isStartLocation;
            floatView.removeView(buttonLayout);
            floatView.addView(buttonLayout, buttonParams);
        });

        // 添加视图
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonLayout.addView(coordinateText, buttonParams);
        buttonLayout.addView(startButton, buttonParams);
//        buttonLayout.addView(mEditText, buttonParams);
//        buttonLayout.addView(mEditTextTime, buttonParams);
        buttonLayout.addView(saveButton, buttonParams);
        buttonLayout.addView(switchLocationButton, buttonParams);
        buttonLayout.addView(endButton, buttonParams);

//        floatView.addView(coordinateText);
        floatView.addView(buttonLayout);

        // 设置触摸监听
        floatView.setOnTouchListener(new FloatViewTouchListener());
    }

    private WindowManager.LayoutParams redDotParams;

    // 初始化红点视图
    private void initRedDotView() {
        redDotView = new View(context);

        // 创建一个圆形drawable
        ShapeDrawable dot = new ShapeDrawable(new OvalShape());
        dot.getPaint().setColor(Color.RED);
        redDotView.setBackground(dot);

        // 设置红点的布局参数
        redDotParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            redDotParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            redDotParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        redDotParams.format = PixelFormat.RGBA_8888;
        redDotParams.gravity = Gravity.START | Gravity.TOP;
        redDotParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        redDotParams.width = 30; // 红点大小
        redDotParams.height = 30; // 红点大小
    }

    public void show(String content) {
        mContent = content;
        try {
            windowManager.addView(floatView, layoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismiss() {
        try {
            if (floatView != null && floatView.isAttachedToWindow()) {
                windowManager.removeView(floatView);
            }
            // 移除红点
            removeRedDot();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int originalWidth;
    private int originalHeight;

    private void toggleRecording() {
        isRecording = !isRecording;
        if (isRecording) {
            // 保存原始尺寸
            originalWidth = layoutParams.width;
            originalHeight = layoutParams.height;
            // 设置为全屏
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.gravity = Gravity.END | Gravity.CENTER;
        } else {
            // 恢复原始尺寸
            layoutParams.width = originalWidth;
            layoutParams.height = originalHeight;
            layoutParams.gravity = Gravity.START | Gravity.TOP;
        }

        if (isRecording) {
            saveButton.setEnabled(true);
            startButton.setText("停止");
            coordinateText.setText("请点击屏幕任意位置");

            // 移除 FLAG_NOT_TOUCH_MODAL 标志，使点击事件不透传给底层 Activity
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            windowManager.updateViewLayout(floatView, layoutParams);

            Toast.makeText(context, "请点击屏幕获取坐标", Toast.LENGTH_SHORT).show();
        } else {
            startButton.setText("开始");
            coordinateText.setText("等待开始...");
            saveButton.setEnabled(false);
            currentPoint = null;

            // 恢复 FLAG_NOT_TOUCH_MODAL 标志，使点击事件可以透传给底层 Activity
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            windowManager.updateViewLayout(floatView, layoutParams);
        }

    }

    private void recordCoordinate(int x, int y) {
        if (isRecording) {
            currentPoint = new Point(x, y);
            coordinateText.setText(String.format(Locale.getDefault(), "坐标: (%d, %d)", x, y));
            saveButton.setEnabled(true);
            // 显示红点
            showRedDotAt(x, y);
        }
    }

    private void saveCoordinate() {
        if (currentPoint != null) {
            final int x = currentPoint.x;
            final int y = currentPoint.y;

            LogControl.d(" saveCoordinate: " +  " " + x + " " + y);

            String string = SPUtils.getInstance().getString(mContent);
            final List<EventClick> eventClicks;
            if (TextUtils.isEmpty(string)) {
                eventClicks = new ArrayList<>();
            } else {
                // [{xxx,xxx},{xxx,xxx}]
                eventClicks = new Gson().fromJson(string, new TypeToken<List<EventClick>>() {
                }.getType());
            }
            eventClicks.add(new EventClick("", new Point(x, y)));

            SPUtils.getInstance().put(mContent, new Gson().toJson(eventClicks));

            toggleRecording();
        }
    }

    // 在指定位置显示红点
    private void showRedDotAt(int x, int y) {
        // 先移除之前的红点
        removeRedDot();

        // 获取状态栏高度
        int statusBarHeight = getStatusBarHeight();

        // 获取导航栏高度
        int navigationBarHeight = getNavigationBarHeight();

        // 计算实际的红点位置
        int actualX = x - redDotParams.width / 2;
        int actualY = y - redDotParams.height / 2;

        // 考虑状态栏偏移（如果坐标系统包含状态栏）
        actualY -= statusBarHeight;

        // 设置红点位置
        redDotParams.x = actualX;
        redDotParams.y = actualY;


        try {
            windowManager.addView(redDotView, redDotParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 移除红点
    private void removeRedDot() {
        try {
            if (redDotView != null && redDotView.isAttachedToWindow()) {
                windowManager.removeView(redDotView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class FloatViewTouchListener implements View.OnTouchListener {
        private int initialX, initialY;
        private float initialTouchX, initialTouchY;
        private boolean isMoving = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = layoutParams.x;
                    initialY = layoutParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    isMoving = false;
                    return true;

                case MotionEvent.ACTION_MOVE:
                    // 移动超过10像素才认为是拖动
                    if (Math.abs(event.getRawX() - initialTouchX) > 10 ||
                            Math.abs(event.getRawY() - initialTouchY) > 10) {
                        isMoving = true;
                        layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        layoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatView, layoutParams);
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    if (!isMoving && isRecording) {
                        // 如果不是拖动且处于记录状态，则记录坐标
                        recordCoordinate((int) event.getRawX(), (int) event.getRawY());
                    }
                    return true;
            }
            return false;
        }
    }

    /**
     * 获取状态栏高度
     */
    private int getStatusBarHeight() {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0; // 默认返回0
    }

    /**
     * 获取导航栏高度
     */
    private int getNavigationBarHeight() {
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0; // 默认返回0
    }

    /**
     * 检查是否有导航栏
     */
    private boolean hasNavigationBar() {
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && context.getResources().getBoolean(id);
    }

}
