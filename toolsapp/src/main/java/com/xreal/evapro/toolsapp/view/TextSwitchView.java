package com.xreal.evapro.toolsapp.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xreal.evapro.toolsapp.util.DensityUtil;
import com.xreal.evapro.toolsapp.util.LogControl;

public class TextSwitchView extends LinearLayout {

    private TextView labelView;
    private View switchThumb;
    private FrameLayout switchWrapper;
    private boolean isChecked = false;
    private OnCheckedChangeListener onCheckedChangeListener;

    private int switchBgWidth = DensityUtil.dip2px(42);
    private int switchBgPadding = DensityUtil.dip2px(2);
    private int thumbWidth = DensityUtil.dip2px(17);
    private int thumbPosition = switchBgWidth - thumbWidth - switchBgPadding * 2;

    public TextSwitchView(Context context) {
        super(context);
        init(context);
    }

    public TextSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TextSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        int padding = 30;
        setPadding(padding, padding, padding, padding);

//        setBackgroundResource(android.R.drawable.dialog_holo_light_frame); // 可自定义背景
//        setBackgroundResource(android.R.drawable.ic_dialog_alert); // 可自定义背景

        labelView = new TextView(context);
        labelView.setText("开关"); // 默认文本
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        labelView.setTextColor(Color.BLACK);
        labelView.setAllCaps(false);
        LayoutParams labelParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        addView(labelView, labelParams);

        // 开关区域（灰色圆角背景 + 白色thumb）
        switchWrapper = new FrameLayout(context);

        int switchHeight = DensityUtil.dip2px(22);
        LayoutParams wrapperParams = new LayoutParams(switchBgWidth, switchHeight);
        wrapperParams.leftMargin = DensityUtil.dip2px(7);
        switchWrapper.setLayoutParams(wrapperParams);
        switchWrapper.setBackground(createRoundedBg(Color.GRAY));
        switchWrapper.setPadding(switchBgPadding, switchBgPadding, switchBgPadding, switchBgPadding);

        // 白色小圆球
        switchThumb = new View(context);
        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(thumbWidth, DensityUtil.dip2px(17));
        thumbParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        switchThumb.setLayoutParams(thumbParams);
        switchThumb.setBackground(createRoundedBg(Color.WHITE));
        switchWrapper.addView(switchThumb);

        addView(switchWrapper);


//        LayoutParams baseParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
////        baseParams.
//        this.setLayoutParams(baseParams);

        setOnClickListener(v -> toggle());
    }

    private GradientDrawable createRoundedBg(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(1000);
        return drawable;
    }

    public void toggle() {
        setChecked(!isChecked);
    }

    public void setChecked(boolean checked) {
        LogControl.d(labelView.getText() + "  isChecked: " + isChecked + ", checked: " + checked);
        if (this.isChecked == checked) return;
        this.isChecked = checked;
        animateSwitch(checked);

        if (onCheckedChangeListener != null) {
            LogControl.d("isChecked=" + isChecked);
            onCheckedChangeListener.onCheckedChanged(this, isChecked);
        }
    }

    private void animateSwitch(boolean checked) {
        int distance = switchWrapper.getWidth() - switchThumb.getWidth() - switchBgPadding * 2; // 预留padding
        LogControl.d("distance: " + distance);
        if (distance < 0) {
            switchThumb.setTranslationX(thumbPosition);
            switchThumb.setBackground(createRoundedBg(isChecked ? Color.CYAN : Color.WHITE));
            return;
        }
        float targetX = checked ? distance : 0;

        ObjectAnimator animator = ObjectAnimator.ofFloat(switchThumb, "translationX", targetX);

        animator.setDuration(200);
        animator.start();

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                switchThumb.setBackground(createRoundedBg(isChecked ? Color.CYAN : Color.WHITE));
            }
        });
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setText(String text) {
        labelView.setText(text);
    }

    public void setTextColor(int color) {
        labelView.setTextColor(color);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.onCheckedChangeListener = listener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(TextSwitchView view, boolean isChecked);
    }
}

