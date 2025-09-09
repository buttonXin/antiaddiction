package com.oldhigh.antiaddiction.activity;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.oldhigh.antiaddiction.action.MiGuAction;
import com.oldhigh.antiaddiction.action.ShoppingAction;
import com.oldhigh.antiaddiction.feature.AddPointFg;
import com.ven.assists.AssistsCore;
import com.ven.assists.stepper.StepManager;

public class HomeAct extends BaseOLActivity {

    private TextView mTextView;
    private boolean isServiceStart = false;

    @Override
    public void initData() {
        mTextView = addText("权限状态");

        addButton("申请权限", view -> {
            mTextView.setText("申请通知消息权限");
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
                mTextView.setText("无障碍服务未开启");
                AssistsCore.INSTANCE.openAccessibilitySetting();
                return;
            }

            // 悬浮窗权限
            if (!Settings.canDrawOverlays(this)) {
                mTextView.setText("悬浮窗权限未开启");
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1001);
                return;
            }
            mTextView.setText("权限全部已开启");
            isServiceStart = true;
            showView();
        });
    }

    private void showView() {

        addButton("开始", view -> {
            StepManager.INSTANCE.execute(ShoppingAction.class, 1, 0, null, true);
        });
        addButton("记录自动化", view -> {
           new AddPointFg()
                .setBaseParams("记录自动化")
                .openFragment(getFragmentManager());
        });
    }
}
