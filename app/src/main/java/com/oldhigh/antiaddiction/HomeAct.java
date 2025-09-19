package com.oldhigh.antiaddiction;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.oldhigh.antiaddiction.activity.BaseOLActivity;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.oldhigh.antiaddiction.feature.operate.PointFg;
import com.oldhigh.antiaddiction.receiver.AdminReceiver;
import com.oldhigh.antiaddiction.util.NotificationHelper;
import com.oldhigh.antiaddiction.util.SPUtils;
import com.ven.assists.AssistsCore;

public class HomeAct extends BaseOLActivity {

    private Button mButtonNotify;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar(getString(R.string.app_name));
    }

    private TextView mTextView;
    private EditText mEditText;

    private boolean hasAbs = false;
    private boolean hasNotify = false;

    @Override
    public void initData() {
        mTextView = addText("权限状态");

        addButton("申请权限", view -> {

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
            showView();
        });
    }

    private void showView() {
        if (hasAbs) {
            return;
        }
        hasAbs = true;

        addButton("查看操作组", view -> {
            new PointFg().openFragment(getFragmentManager());
        });
        addLine();
        final boolean aBoolean = SPUtils.getInstance().getBoolean(SpKey.KEY_Notification);
        addSwitch("是否启动通知栏执行-操作组", aBoolean, (view, isChecked) -> {
            SPUtils.getInstance().put(SpKey.KEY_Notification, isChecked);
            mEditText.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            mButtonNotify.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        mEditText = addEditText("输入操作组名称", 1);
        mButtonNotify = addButton("测试通知栏执行", 1, view -> {
            final String string = mEditText.getText().toString();
            if (TextUtils.isEmpty(string)) {
                toast("请输入内容");
                return;
            }
            NotificationHelper.sendNotification(getApplicationContext(), string);
        });
        if (!aBoolean) {
            mEditText.setVisibility(View.GONE);
            mButtonNotify.setVisibility(View.GONE);
            addButton("申请通知栏权限", view -> {
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
                }
            });
        }

        addButton("锁屏权限", view -> {
            ComponentName componentName = new ComponentName(this, AdminReceiver.class);
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "--设备管理器--");
            startActivityForResult(intent, 0);
        });


    }
}
