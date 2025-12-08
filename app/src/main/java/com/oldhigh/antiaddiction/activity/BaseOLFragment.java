package com.oldhigh.antiaddiction.activity;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Size;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.oldhigh.antiaddiction.IResult;
import com.oldhigh.antiaddiction.R;
import com.oldhigh.antiaddiction.util.DensityUtil;
import com.oldhigh.antiaddiction.util.LogControl;
import com.oldhigh.antiaddiction.view.TextSwitchView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseOLFragment extends Fragment {

    protected String TAG = BaseOLFragment.class.getSimpleName();

    private LinearLayout llContent;

    protected Handler handler = new Handler(Looper.getMainLooper());
    protected FrameLayout mFrameLayout;
    public Activity mActivity;
    private View mDecorView;
    private OnDestroyListener mDestroyListener;
    private IResult<Boolean> mIResult;

    // 距离下面view的边距
    public int getBottomMargin() {
        return 20;
    }

    public abstract void initData();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        mActivity = getActivity();

        if (hasFullScreen()) {
            mDecorView = mActivity.getWindow().getDecorView();
            fullScreen();
            // 设置监听系统 UI 可见性变化
            mDecorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                boolean isFullscreen = (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
                if (!isFullscreen) {
                    // 用户下拉状态栏后延迟再次隐藏
                    handler.postDelayed(this::fullScreen, 3000);
                }
            });
        }
    }

    /**
     * 沉浸式全屏
     */
    private void fullScreen() {

        // Hide the status bar.
        // Hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;

        if (mDecorView != null) {
            mDecorView.setSystemUiVisibility(uiOptions);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (mDecorView != null && hasFullScreen()) {
            mDecorView.setOnSystemUiVisibilityChangeListener(null);
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
        if (mDestroyListener != null) {
            mDestroyListener.onDestroy();
        }

    }

    /**
     * 黑色背景,就不需要增加addBg();
     */
    protected boolean isBlackScreen() {
        return false;
    }

    protected boolean hasFullScreen() {
        return false;
    }

    public BaseOLFragment setBaseParams(String content) {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        args.putString("content", content);
        setArguments(args);
        return this;
    }

    public BaseOLFragment setBaseParams2(String content) {
        final Bundle args = getArguments() == null ? new Bundle() : getArguments();
        args.putString("content2", content);
        setArguments(args);
        return this;
    }

    protected String getContent() {
        final Bundle args = getArguments();
        if (args != null) {
            return args.getString("content");
        }
        return "";
    }

    protected String getContent2() {
        final Bundle args = getArguments();
        if (args != null) {
            return args.getString("content2");
        }
        return "";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context = container.getContext();
        mFrameLayout = new FrameLayout(context);
        ScrollView scrollView = new ScrollView(context);
        llContent = new LinearLayout(context);
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setGravity(Gravity.CENTER);

        scrollView.addView(llContent);
        mFrameLayout.addView(scrollView, 0);
        mFrameLayout.setBackgroundColor(isBlackScreen() ? Color.BLACK : Color.WHITE);

        addTitleBar();

        if (!isBlackScreen()) {
            addBg();
        }

        initData();

        return mFrameLayout;
    }

    protected List<View> mLLViews = new ArrayList<>();
    protected List<View> mLLHViews = new ArrayList<>();

    /**
     * 移除 正常view
     */
    protected void removeLLView() {
        for (View llView : mLLViews) {
            removeLLView(llView);
        }
        mLLViews.clear();
    }

    protected void removeLLView(View view) {
        try {
            llContent.removeView(view);
        } catch (Exception e) {
            LogControl.e(TAG, "removeView: " + e.getMessage());
        }
    }

    /**
     * 移除横向view
     */
    protected void removeLLHView(int index) {

        for (View llView : mLLHViews) {
            removeLLHView(index, llView);
        }
        mLLHViews.clear();
    }

    protected void removeLLHView(int index, View view) {
        try {
            final LinearLayout linearLayout = mLlHorizontalMap.get(index);
            linearLayout.removeView(view);
        } catch (Exception e) {
            LogControl.e(TAG, "removeView: " + e.getMessage());
        }
    }

    private void addBg() {
        ImageView view = new ImageView(mActivity);

        view.setImageDrawable(mActivity.getDrawable(R.drawable.ol_bg));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        view.setScaleType(ImageView.ScaleType.FIT_XY);
        mFrameLayout.addView(view, 0);
    }

    private void addTitleBar() {
        addTitleBar(getClass().getSimpleName() + " - page");
    }

    protected void addTitleBar(String text) {

        if (TextUtils.isEmpty(text)) {
            return;
        }
        final TextView view = new TextView(mActivity);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 30, 0, 30);
        view.setLayoutParams(layoutParams);
        view.setText(text);
        view.setTextColor(isBlackScreen() ? Color.WHITE : Color.BLACK);
        view.setGravity(Gravity.CENTER);
        view.setTextSize(20);
        llContent.addView(view);
        view.setOnClickListener(v -> getActivity().onBackPressed());
        if (!TextUtils.isEmpty(text)) {
            final View viewLine = new View(llContent.getContext());
            final LinearLayout.LayoutParams layoutParamsLine = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2);
            layoutParamsLine.setMargins(20, 0, 20, getBottomMargin());
            viewLine.setLayoutParams(layoutParamsLine);
            viewLine.setBackgroundColor(Color.parseColor("#63999999"));
            llContent.addView(viewLine);
        }
    }


    /**
     * 打开fg
     */
    public BaseOLFragment openFragment(FragmentManager fragmentManager) {
        fragmentManager.beginTransaction().add(android.R.id.content, this, this.getClass().getSimpleName())
                .addToBackStack(this.getClass().getSimpleName()).commit();
        return this;
    }

    public BaseOLFragment openFragment(FragmentManager fragmentManager, IResult<Boolean> iResult) {
        fragmentManager.beginTransaction().add(android.R.id.content, this, this.getClass().getSimpleName())
                .addToBackStack(this.getClass().getSimpleName()).commit();
        mIResult = iResult;
        return this;
    }

    public void closeFragment() {
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    public BaseOLFragment setOnDestroyListener(OnDestroyListener listener) {
        mDestroyListener = listener;
        return this;
    }


    public void removeFragment() {
        getActivity().getFragmentManager().beginTransaction().remove(this).commit();
    }

    public interface OnDestroyListener {
        void onDestroy();
    }


    public Button addButton(String name, View.OnClickListener listener) {

        Button button = new Button(llContent.getContext());
        button.setText(name);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.button_selector);
        int padding = DensityUtil.dip2px(10);
        button.setPadding(padding, padding, padding, padding);
        if (listener != null) {
            button.setOnClickListener(v -> {
                LogControl.d(TAG, name + " onClick: ");
                listener.onClick(v);
            });
        }

        addLlView(button);

        return button;
    }

    public Button addButton(String name, int index, View.OnClickListener listener) {

        Button button = new Button(llContent.getContext());
        button.setText(name);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        button.setAllCaps(false);
        button.setBackgroundResource(R.drawable.button_selector);
        int padding = DensityUtil.dip2px(10);
        button.setPadding(padding, padding, padding, padding);
        if (listener != null) {
            button.setOnClickListener(v -> {
                LogControl.d(TAG, name + " onClick: ");
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
        addLlView(editText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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
        view.setTextColor(isBlackScreen() ? Color.WHITE : Color.BLACK);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        view.setAllCaps(false);
        if (listener != null) {
            view.setOnClickListener(v -> {
                LogControl.d(TAG, name + " onClick: ");
                listener.onClick(v);
            });
        }
        addLlView(view);

        return view;
    }

    public TextView addText(String name, int index, View.OnClickListener listener) {
        TextView view = new TextView(llContent.getContext());

        view.setText(name);
        view.setTextColor(isBlackScreen() ? Color.WHITE : Color.BLACK);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        view.setAllCaps(false);
        if (listener != null) {
            view.setOnClickListener(v -> {
                LogControl.d(TAG, name + " onClick: ");
                listener.onClick(v);
            });
        }
        addHorizontalLlView(view, index);

        return view;
    }

    public ImageView addImage(int id) {
        return addImage(getActivity().getDrawable(id), new Size(150, 150));
    }

    public ImageView addImage(Drawable drawable, Size roundSize) {
        ImageView view = new ImageView(llContent.getContext());

        view.setImageDrawable(drawable);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(roundSize.getWidth(), roundSize.getHeight());

        addLlView(view, params);

        return view;
    }

    public ImageView addImage(int id, int index) {
        return addImage(getActivity().getDrawable(id), index, new Size(150, 150));
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(roundSize.getWidth(), roundSize.getHeight());
        addHorizontalLlView(view, params, index);

        return view;
    }

    public TextSwitchView addSwitch(String text, boolean checked, TextSwitchView.OnCheckedChangeListener listener) {
        TextSwitchView view = new TextSwitchView(llContent.getContext());
        view.setText(text);
        view.setTextColor(isBlackScreen() ? Color.WHITE : Color.BLACK);
        view.setChecked(checked);
        view.setOnCheckedChangeListener(listener);
        addLlView(view);
        return view;
    }

    public TextSwitchView addSwitch(String text, int index, boolean checked, TextSwitchView.OnCheckedChangeListener listener) {
        TextSwitchView view = new TextSwitchView(llContent.getContext());
        view.setText(text);
        view.setTextColor(isBlackScreen() ? Color.WHITE : Color.BLACK);
        view.setChecked(checked);
        view.setOnCheckedChangeListener(listener);
        addHorizontalLlView(view, index);
        return view;
    }

    public void addLine() {
        final View view = new View(llContent.getContext());
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        layoutParams.setMargins(20, 0, 20, 0);
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(Color.parseColor("#63999999"));
        llContent.addView(view);
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
            HorizontalScrollView scrollView = new HorizontalScrollView(getActivity());
            llView = new LinearLayout(getActivity());
            llView.setOrientation(LinearLayout.HORIZONTAL);
            llView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.leftMargin = 20;
            params.topMargin = 20;
            params.bottomMargin = 20;
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
            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        params.rightMargin = 20;
        view.setLayoutParams(params);
        linearLayout.addView(view);
    }

    public void clearHorizontalLlView(int index) {
        LinearLayout linearLayout = mLlHorizontalMap.get(index);
        if (linearLayout != null) {
            linearLayout.removeAllViews();
            llContent.removeView(linearLayout);
        }
    }

    public void addLlView(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = getBottomMargin();
        params.leftMargin = getBottomMargin();
        params.rightMargin = getBottomMargin();
        addLlView(view, params);
    }

    private void addLlView(View view, LinearLayout.LayoutParams params) {
        params.bottomMargin = getBottomMargin();
        view.setLayoutParams(params);
        llContent.addView(view);
    }

    public View addFullscreenView(View view) {
        mFrameLayout.addView(view);
        return view;
    }

    protected void toast(int resId) {
        toast(getString(resId));
    }

    protected void toast(String text) {
        try {
            if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                Toast.makeText(ActivityLifecycleHelper.getActivity(), text, Toast.LENGTH_SHORT).show();
            } else {
                handler.post(() -> Toast.makeText(ActivityLifecycleHelper.getActivity(), text, Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            LogControl.d(TAG, "toast: " + e.getMessage());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        LogControl.d(TAG, "onPause: ");
        if (mIResult != null) {
            mIResult.onResult(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LogControl.d(TAG, "onResume: ");
    }

}
