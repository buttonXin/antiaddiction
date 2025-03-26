package com.xreal.evapro.toolsapp.base;

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

import com.xreal.evapro.toolsapp.util.LogControl;

import android.util.Size;
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

import com.xreal.evapro.toolsapp.ActivityLifecycleHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseOLFragment extends Fragment {

    protected String TAG = BaseOLFragment.class.getSimpleName();

    private LinearLayout llContent;

    protected Handler handler = new Handler(Looper.getMainLooper());
    private FrameLayout mFrameLayout;
    public Activity mActivity;

    // 距离下面view的边距
    public int getBottomMargin() {
        return 40;
    }

    public abstract void initData();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        mActivity = getActivity();

    }

    protected boolean isBlackScreen() {
        return false;
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


        initData();

        return mFrameLayout;
    }

    private void addTitleBar() {
        addTitleBar(getClass().getSimpleName() + " - page");
    }

    protected void addTitleBar(String text) {

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
    public void openFragment(FragmentManager fragmentManager) {
        fragmentManager.beginTransaction().add(android.R.id.content, this).addToBackStack(null).commit();
    }

    public void removeFragment() {
        getActivity().getFragmentManager().beginTransaction().remove(this).commit();
    }
//    public <T extends BaseOLFragment> void openFragment(T fragment) {
//        getActivity().getSupportFragmentManager().beginTransaction()
//                .replace(android.R.id.content, fragment).addToBackStack(null).commit();
//    }
//
//    public static <T extends BaseOLFragment> void openFragment(FragmentActivity activity, Class<T> clazz) {
//
//        try {
//            activity.getSupportFragmentManager().beginTransaction()
//                    .replace(android.R.id.content, clazz.newInstance())
//                    .addToBackStack(null)
//                    .commit();
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (java.lang.InstantiationException e) {
//            throw new RuntimeException(e);
//        }
//    }


    public Button addButton(String name, View.OnClickListener listener) {

        Button button = new Button(llContent.getContext());
        button.setText(name);
        button.setAllCaps(false);
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
        button.setAllCaps(false);
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

    protected void toast(int resId) {
        toast(getString(resId));
    }

    protected void toast(String text) {
        try {
            handler.post(() -> Toast.makeText(ActivityLifecycleHelper.getActivity(), text, Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            LogControl.d(TAG, "toast: " + e.getMessage());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        LogControl.d(TAG, "onPause: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        LogControl.d(TAG, "onResume: ");
    }

}
