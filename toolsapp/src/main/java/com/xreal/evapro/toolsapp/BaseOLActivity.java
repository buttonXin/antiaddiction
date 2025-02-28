package com.xreal.evapro.toolsapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseOLActivity extends Activity {

    protected static String TAG = BaseOLActivity.class.getSimpleName();
    private LinearLayout llContent;

    protected Handler handler = new Handler(Looper.getMainLooper());
    private FrameLayout mFrameLayout;

    // 距离下面view的边距
    public int getBottomMargin() {
        return 40;
    }

    public abstract void initData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();

        // 状态栏 通知栏颜色反转
//        int statusFlag = -1;
//        int navigationFlag = -1;
//        statusFlag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//        navigationFlag = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
//
//        if (statusFlag != -1 && navigationFlag != -1) {
//            getWindow().getDecorView().setSystemUiVisibility(statusFlag | navigationFlag);
//        } else if (statusFlag != -1) {
//            getWindow().getDecorView().setSystemUiVisibility(statusFlag);
//        } else if (navigationFlag != -1) {
//            getWindow().getDecorView().setSystemUiVisibility(navigationFlag);
//        } else {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        transparentNavBar(this);
        mFrameLayout = new FrameLayout(this);
        ScrollView scrollView = new ScrollView(this);
        llContent = new LinearLayout(this);
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setGravity(Gravity.CENTER);

        addTitleBar();

        final View view = new View(this);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 30, 0, 30);
        view.setLayoutParams(layoutParams);
        llContent.addView(view);
        scrollView.addView(llContent);
        mFrameLayout.addView(scrollView, 0);
        mFrameLayout.setBackgroundColor(Color.WHITE);
        setContentView(mFrameLayout);

        initData();
    }

    private void addTitleBar() {
        addTitleBar(getClass().getSimpleName() + " - page");
    }

    protected void addTitleBar(String text) {

        final TextView view = new TextView(this);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 30, 0, 30);
        view.setLayoutParams(layoutParams);
        view.setText(text);
        view.setTextColor(Color.BLACK);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(20);
        llContent.addView(view);
    }


    public <T extends Activity> void startAct(Class<T> cls) {
        startActivity(new Intent(getApplicationContext(), cls));
    }

    public Button addButton(String name, View.OnClickListener listener) {

        Button button = new Button(llContent.getContext());
        button.setText(name);
        button.setAllCaps(false);
        if (listener != null) {
            button.setOnClickListener(v -> {
                Log.v(TAG, name + " onClick: ");
                listener.onClick(v);
            });
        }

        addLlView(button);

        return button;
    }

    public Button addButton(String name, int index, View.OnClickListener listener) {

        Button button = new Button(llContent.getContext());
        button.setText(name);
        button.setAllCaps(false);
        if (listener != null) {
            button.setOnClickListener(v -> {
                Log.v(TAG, name + " onClick: ");
                listener.onClick(v);
            });
        }

        addHorizontalLlView(button, index);

        return button;
    }

    public EditText addEditText(String hint) {

        EditText editText = new EditText(llContent.getContext());
        editText.setHint(hint);
        editText.setAllCaps(false);
        addLlView(editText, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return editText;
    }

    public EditText addEditText(String hint, int index) {

        EditText editText = new EditText(llContent.getContext());
        editText.setHint(hint);
        editText.setAllCaps(false);
        addHorizontalLlView(editText, index);
        return editText;
    }

    public TextView addText(String name) {

        return addText(name, null);
    }

    public TextView addText(String name, int index) {

        return addText(name, index, null);
    }

    public TextView addText(String name, View.OnClickListener listener) {
        TextView view = new TextView(llContent.getContext());
        view.setText(name);
        view.setAllCaps(false);
        if (listener != null) {
            view.setOnClickListener(v -> {
                Log.v(TAG, name + " onClick: ");
                listener.onClick(v);
            });
        }
        addLlView(view);

        return view;
    }

    public TextView addText(String name, int index, View.OnClickListener listener) {
        TextView view = new TextView(llContent.getContext());
        view.setText(name);
        view.setAllCaps(false);
        if (listener != null) {
            view.setOnClickListener(v -> {
                Log.v(TAG, name + " onClick: ");
                listener.onClick(v);
            });
        }
        addHorizontalLlView(view, index);

        return view;
    }

    public ImageView addImage(int id) {
        return addImage(getDrawable(id), new Size(150, 150));
    }

    public ImageView addImage(Drawable drawable, Size roundSize) {
        ImageView view = new ImageView(llContent.getContext());

        view.setImageDrawable(drawable);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                roundSize.getWidth(),
                roundSize.getHeight());

        addLlView(view, params);

        return view;
    }

    public ImageView addImage(int id, int index) {
        return addImage(getDrawable(id), index, new Size(150, 150));
    }

    /**
     * 添加到某一行的view中
     */
    public ImageView addImage(Drawable drawable, int index) {
        return addImage(drawable, index, new Size(150, 150));
    }

    public ImageView addImage(Drawable drawable, int index, Size roundSize) {
        ImageView view = new ImageView(llContent.getContext());

        if (drawable != null) {
            view.setImageDrawable(drawable);

        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                roundSize.getWidth(),
                roundSize.getHeight());
        addHorizontalLlView(view, params, index);

        return view;
    }

    private final Map<Integer, LinearLayout> mLlHorizontalMap = new HashMap<>();


    /**
     * 水平布局的整体点击事件
     */
    public void onHorizontalLlViewClick(int index, View.OnClickListener listener) {

        getHorizontalLlView(index).setOnClickListener(listener);
    }

    private LinearLayout getHorizontalLlView(int index) {
        LinearLayout llView = mLlHorizontalMap.get(index);
        if (llView == null) {
            HorizontalScrollView scrollView = new HorizontalScrollView(this);
            llView = new LinearLayout(this);
            llView.setOrientation(LinearLayout.HORIZONTAL);
            llView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 20;
            params.topMargin = getBottomMargin();
            params.bottomMargin = getBottomMargin();
            scrollView.addView(llView, params);
            scrollView.setBackgroundColor(Color.parseColor("#1A3F3F3F"));
            addLlView(scrollView);
            mLlHorizontalMap.put(index, llView);
        }
        return llView;
    }

    private void addHorizontalLlView(View view, int index) {
        addHorizontalLlView(view, null, index);
    }

    private void addHorizontalLlView(View view, LinearLayout.LayoutParams params, int index) {
        LinearLayout linearLayout = mLlHorizontalMap.get(index);
        if (linearLayout == null) {
            linearLayout = getHorizontalLlView(index);
        }
        if (params == null) {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        params.rightMargin = 20;
        view.setLayoutParams(params);
        linearLayout.addView(view);
    }

    public void addLlView(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = getBottomMargin();
        addLlView(view, params);
    }

    private void addLlView(View view, LinearLayout.LayoutParams params) {
        params.bottomMargin = getBottomMargin();
        view.setLayoutParams(params);
        llContent.addView(view);
    }

    public View addFullscreenView(View view) {
        mFrameLayout.addView(view, 0);
        return view;
    }


    public static void transparentNavBar(final Activity activity) {
        transparentNavBar(activity.getWindow());
    }

    public static void transparentNavBar(final Window window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }
        window.setNavigationBarColor(Color.TRANSPARENT);
        View decorView = window.getDecorView();
        int vis = decorView.getSystemUiVisibility();
        int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        decorView.setSystemUiVisibility(vis | option);
    }

    protected void toast(int resId) {
        toast(getString(resId));
    }

    protected void toast(String text) {
        runOnUiThread(() -> Toast.makeText(getBaseContext(), text, Toast.LENGTH_SHORT).show());
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");
    }
}

