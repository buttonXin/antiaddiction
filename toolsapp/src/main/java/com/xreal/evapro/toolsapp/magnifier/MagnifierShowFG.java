package com.xreal.evapro.toolsapp.magnifier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.DensityUtil;
import com.xreal.evapro.toolsapp.util.LogControl;

public class MagnifierShowFG extends BaseOLFragment {

    private int textSize = 20;
    private TextView mTextView;
    private EditText mEditText;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("");
    }

    @Override
    protected boolean hasFullScreen() {
        return true;
    }

    @Override
    public void initData() {

        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        addFullTV();


        addBottom();

    }


    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f; // 默认缩放比例
    private float minFontSize = 20f;
    private float maxFontSize = 50f;

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // 缩放比例叠加
            scaleFactor *= detector.getScaleFactor();

            // 限制缩放范围
            scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 3.0f));

            // 根据比例设置字体大小
            float newSize = 18f * scaleFactor;
            newSize = Math.max(minFontSize, Math.min(newSize, maxFontSize));

            final int size = DensityUtil.dip2px(newSize);
            LogControl.d("newSize:" + newSize, "size:" + size);
            mTextView.setTextSize(size);

            return true;
        }
    }

    private void addBottom() {
        LinearLayout llView = new LinearLayout(getActivity());
        llView.setOrientation(LinearLayout.HORIZONTAL);
        llView.setGravity(Gravity.END | Gravity.BOTTOM);
        addFullscreenView(llView);


        final LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT);
        etParams.rightMargin = getBottomMargin() * 2;
        etParams.bottomMargin = 30;
        etParams.weight = 1;

        mEditText = new EditText(mActivity);
        mEditText.setHint("输入内容");
        mEditText.setBackgroundColor(Color.TRANSPARENT);

        // 监听键盘的完成事件
        mEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mEditText.setVisibility(View.GONE);
                final String string = mEditText.getText().toString();
                LogControl.d("string:" + string);
                mTextView.setText(string);
                return false;
            }
            return false;
        });


        mEditText.postDelayed(() -> showInputTips(mEditText), 200);


        Button button2 = new Button(mActivity);
        button2.setText("  输入  ");
        button2.setAllCaps(false);
        button2.setOnClickListener(v -> {

            showInputTips(mEditText);
        });
        final LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.rightMargin = getBottomMargin() * 2;
        btnParams.bottomMargin = 30;
        llView.addView(mEditText, 0, etParams);
        llView.addView(button2, 1, btnParams);
    }

    private void showInputTips(EditText et_text) {
        et_text.setVisibility(View.VISIBLE);
        et_text.setFocusable(true);
        et_text.setFocusableInTouchMode(true);
        et_text.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) et_text.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(et_text, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addFullTV() {
        mTextView = new TextView(mActivity);

        final String content = getContent();
        mTextView.setText(content);
        mTextView.setGravity(Gravity.CENTER);

        mTextView.setTextColor(Color.BLACK);
        mTextView.setAllCaps(false);
        mTextView.setTextSize(DensityUtil.dip2px(textSize));
        mTextView.setPadding(20, 20, 20, 20);
        addFullscreenView(mTextView);

        scaleGestureDetector = new ScaleGestureDetector(mActivity, new ScaleListener());
        mTextView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            return true;
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


}
