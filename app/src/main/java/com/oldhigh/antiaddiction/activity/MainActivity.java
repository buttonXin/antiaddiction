package com.oldhigh.antiaddiction.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hjq.toast.ToastUtils;
import com.oldhigh.antiaddiction.R;
import com.oldhigh.antiaddiction.service.AntiAddictionService;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LinearLayout llContent;
    private View viewChoose;
    private View viewSelected;
    private View viewAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llContent = findViewById(R.id.ll_content);


        initData();
    }

    private boolean isServiceStart = false;

    private void initData() {

        addText("点击无障碍，然后选择Anti-Addiction应用，将开关开启；" +
                "\n之后 点击选择应用 ，然后选择要防沉迷的应用，文字变红表示选中,最后点击下面的保存即可；" +
                "\n可以 点击查看应用，来查看选择了那些应用；" +
                "\n目前选择的应用是30分钟会进行提醒。");


        addButton("开启无障碍", view -> {
            checkService();

        });


        viewChoose = addButton("选择应用", view -> startActivity(
                new Intent(getApplicationContext(), ChooseActivity.class)));

        viewSelected = addButton("查看应用", view -> startActivity(
                new Intent(getApplicationContext(), SelectedActivity.class)));


        viewAd = addButton("添加广告", view -> startActivity(
                new Intent(getApplicationContext(), EditAdActivity.class)));


        showButton();

    }

    private void checkService() {
        if (!isAccessibilitySettingsOn(this,
                AntiAddictionService.class.getName())) {// 判断服务是否开启
            jumpToSettingPage(this);// 跳转到开启页面
        } else {
            isServiceStart = true;
            ToastUtils.show("服务已开启，点击选择应用");
            showButton();
        }
    }

    private void showButton() {
        if (isServiceStart) {
            viewChoose.setVisibility(View.VISIBLE);
            viewSelected.setVisibility(View.VISIBLE);
            viewAd.setVisibility(View.VISIBLE);
        } else {
            viewChoose.setVisibility(View.GONE);
            viewSelected.setVisibility(View.GONE);
            viewAd.setVisibility(View.GONE);
        }
    }

    private View addButton(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 40;
        params.bottomMargin = 40;
        button.setOnClickListener(listener);
        llContent.addView(button, params);
        return button;
    }


    private View addText(String text) {
       return addText(text, v -> {
        });
    }

    private View addText(String text, View.OnClickListener listener) {
        TextView textView = new TextView(this);
        textView.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 40;
        params.topMargin = 40;
        params.rightMargin = 40;
        params.bottomMargin = 40;
        params.gravity= Gravity.CENTER;
        textView.setOnClickListener(listener);
        llContent.addView(textView, params);
        return textView;
    }


    /**
     * 跳转到无障碍服务设置页面
     *
     * @param context 设备上下文
     */
    public static void jumpToSettingPage(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * 判断是否有辅助功能权限
     *
     * @return true 已开启
     * false 未开启
     */
    public static boolean isAccessibilitySettingsOn(Context context, String className) {
        if (context == null) {
            return false;
        }
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices =
                activityManager.getRunningServices(100);// 获取正在运行的服务列表
        if (runningServices.size() < 0) {
            return false;
        }
        for (int i = 0; i < runningServices.size(); i++) {
            ComponentName service = runningServices.get(i).service;
            if (service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }
}