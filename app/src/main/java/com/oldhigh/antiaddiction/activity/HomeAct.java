package com.oldhigh.antiaddiction.activity;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.oldhigh.antiaddiction.R;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.oldhigh.antiaddiction.feature.operate.PointFg;
import com.oldhigh.antiaddiction.util.NotificationHelper;
import com.oldhigh.antiaddiction.util.SPUtils;
import com.ven.assists.AssistsCore;

public class HomeAct extends BaseOLActivity {

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar(getString(R.string.app_name));
    }
    private TextView mTextView;
    private boolean isServiceStart = false;
    private EditText mEditText;

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

        addButton("查看操作组", view -> {
            new PointFg().openFragment(getFragmentManager());
        });
        final boolean aBoolean = SPUtils.getInstance().getBoolean(SpKey.KEY_Notification);
        addSwitch("是否启动通知栏执行", aBoolean, (view, isChecked) -> {
            SPUtils.getInstance().put(SpKey.KEY_Notification, isChecked);
        });
        mEditText = addEditText("输入操作组名称", 1);
        addButton("测试通知栏执行", 1, view -> {
            final String string = mEditText.getText().toString();
            if (TextUtils.isEmpty(string)) {
                toast("请输入内容");
                return;
            }
            NotificationHelper.sendNotification(getApplicationContext(), string);
        });
    }
}
