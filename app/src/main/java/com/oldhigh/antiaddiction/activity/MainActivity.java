package com.oldhigh.antiaddiction.activity;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hjq.toast.ToastUtils;
import com.oldhigh.antiaddiction.R;
import com.oldhigh.antiaddiction.action.MiGuAction;
import com.oldhigh.antiaddiction.action.ShoppingAction;
import com.oldhigh.antiaddiction.receiver.AdminReceiver;
import com.oldhigh.antiaddiction.service.AntiAddictionService;
import com.oldhigh.antiaddiction.util.FloatPointHelper;
import com.ven.assists.AssistsCore;
import com.ven.assists.stepper.StepManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
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


        viewChoose = addButton("选择应用", view -> {
            final FloatPointHelper floatPointHelper = new FloatPointHelper(this);
            floatPointHelper.show();
        });

        viewSelected = addButton("查看应用", view -> {
            view.postDelayed(() -> {
//从MyStepImpl步骤1开始执行，isBegin是否作为起始步骤，默认false
//                StepManager.execute(MyStepImpl::class.java, 1, isBegin = true)
                StepManager.INSTANCE.execute(MiGuAction.class, 1, 0, null, true);
            }, 5000);
        });


        viewAd = addButton("通知权限", view -> {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        });

        viewAd = addButton("锁屏权限", view -> {
            ComponentName componentName = new ComponentName(this, AdminReceiver.class);
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "--设备管理器--");
            startActivityForResult(intent, 0);
        });


        showButton();

    }

    private void checkService() {


        // 监听通知权限
        String enabledListeners = Settings.Secure.getString(
                getContentResolver(),
                "enabled_notification_listeners"
        );
        String packageName = getPackageName();
        final boolean notifyPer = enabledListeners != null && enabledListeners.contains(packageName);
        Log.e(TAG, "checkService: " + notifyPer);
        if (!notifyPer) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
            return;
        }
        // 判断无障碍服务是否开启
        if (!AssistsCore.INSTANCE.isAccessibilityServiceEnabled()) {
            AssistsCore.INSTANCE.openAccessibilitySetting();
            return;
        }

        // 悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 1001);
            return;
        }

        isServiceStart = true;
        showButton();

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
        params.gravity = Gravity.CENTER;
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