package com.xreal.evapro.toolsapp.local_server;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xreal.evapro.toolsapp.R;
import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.DensityUtil;
import com.xreal.evapro.toolsapp.util.LogControl;
import com.xreal.evapro.toolsapp.util.SPUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class LocalServerFG extends BaseOLFragment {

    private boolean isOnlyDownload;
    private ImageView mImageView;

    // 访问密码设置(明文保存,方便查看;哈希在开启服务时计算并传给服务器校验)
    private static final String SP_KEY_PASSWORD = "local_server_password";
    private TextView mPasswordStatus;
    private EditText mPasswordEdit;
    // 密码按钮随状态切换:未设置密码时是"保存密码",已设置时是"清空"
    private Button mPasswordButton;

    // 横向布局的行号:同一行号的控件会排在一行
    private static final int ROW_PASSWORD = 1;
    private static final int ROW_SERVER = 2;
    private static final int ROW_NOTICE = 3;

    // 开启服务与停止服务共用一个按钮
    private Button mServerButton;
    private boolean isServerRunning;

    // 网页顶部提示:改动实时生效,服务器线程也会读,故用 volatile
    private EditText mNoticeEdit;
    private volatile String mNoticeText = "";

    // 网页端点"发送到手机"发来的文字:显示在这里方便复制
    private TextView mPushedText;
    // 纯文本值,避免把占位提示当成内容复制走
    private volatile String mPushedTextValue = "";

    private static final String PUSHED_TEXT_EMPTY_HINT = "等待网页发送内容…";

    public LocalServerFG() {
    }

    private static final String TAG = LocalServerFG.class.getSimpleName();
    private TextView mTextView;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("开启本地服务");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LogControl.d(TAG, "onActivityResult: " + requestCode);
        if (requestCode == 110) {
            startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            toast("权限已经允许");
        }
    }

    @Override
    public void initData() {

        isOnlyDownload = getContent().equals("true");
        mTextView = addText("");
        mImageView = addImage(getResources().getDrawable(R.mipmap.ic_launcher), new Size(500, 500));
        mImageView.setVisibility(View.GONE);

        // 访问密码:必须在开启服务前设置好,保存后需重新开启服务才生效
        // 状态文本与下面的密码行紧挨着,中间不加分割线
        mPasswordStatus = addTextNoLine("");
        mPasswordEdit = addEditText("访问密码", ROW_PASSWORD);
        mPasswordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        // 横向布局里宽度是 wrap_content,给个最小宽度避免输入框太窄
        mPasswordEdit.setMinWidth(DensityUtil.dip2px(160));
        mPasswordButton = addButton("保存密码", ROW_PASSWORD, v -> onPasswordButtonClick());
        updatePasswordStatus();

        // 申请权限与开启服务放在同一行
        addButton("申请权限", ROW_SERVER, v -> {
            // 动态申请权限
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 110);
        });
        // 开启服务与停止服务合并成一个按钮:点击切换
        mServerButton = addButton("开启服务", ROW_SERVER, v -> toggleServer());

        // 网页顶部提示:默认取剪切板内容,改动实时同步到网页
        mNoticeEdit = addEditText("网页顶部提示(默认取剪切板)", ROW_NOTICE);
        mNoticeEdit.setMinWidth(DensityUtil.dip2px(220));
        // 默认取剪切板内容。这里必须同时写入 mNoticeText:服务器读的是它，
        // setText 发生在监听器注册之前不会触发回调，只设 EditText 的话网页上看不到。
        mNoticeText = CopyClipToFileFG.getClipContent(getContext());
        mNoticeEdit.setText(mNoticeText);
        mNoticeEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mNoticeText = (s == null) ? "" : s.toString();
            }
        });
        addButton("清空", ROW_NOTICE, v -> mNoticeEdit.setText(""));

        // 网页端发来的文字:动态监听,收到就显示在这里;与下面的复制按钮之间不加分割线
        mPushedText = addTextNoLine(PUSHED_TEXT_EMPTY_HINT);
        addButton("复制网页发来的内容", v -> copyPushedText());

        addButton("保存内容到-下载", v -> {
            ApkExporter.exportApkToDownloads(mActivity);
            toast("保存成功");
        });
        addText(" 电脑开启本地服务" +
                "\n python3 -m http.server 8080" +
                "\n python2 -m SimpleHTTPServer 8080" +
                "\n 浏览器输入 http://<电脑IP地址>:8080 即可访问电脑文件 " +
                "\n Mac: ifconfig       Windows: ipconfig" +
                "\n Mac: 用户与群组 客人用户 点击开启共享" +
                "\n Windows: 控制面板-防火墙里先临时关闭");


    }

    private static final int PORT = 8080;
    private static final int REQUEST_PERMISSION_CODE = 100;
    private FileServer fileServer;

    private String localIpAddress;

    @Override
    protected boolean isBlackScreen() {
        return true;
    }


    private void toggleServer() {
        if (isServerRunning) {
            stopServer();
            mTextView.setText("服务已经停止");
        } else {
            startServer();
        }
    }

    private void startServer() {
        try {
            // 密码只在开启服务前设置才生效:启动时读取已保存的明文并计算哈希,运行中改密码需重启服务
            String pwd = SPUtils.getInstance().getString(SP_KEY_PASSWORD, "");
            String pwdHash = TextUtils.isEmpty(pwd) ? "" : FileServer.sha256Hex(pwd);
            // 顶部提示实时读取 mNoticeText:改输入框内容后,网页刷新即可看到
            // 网页端点"发送到手机"时回调在服务器线程,切回主线程再更新界面
            fileServer = new FileServer(PORT, getActivity(), isOnlyDownload, pwdHash, () -> mNoticeText,
                    text -> handler.post(() -> updatePushedText(text)));
            fileServer.start();
            String ipAddress = getIPAddress(true); // 获取本地IP
            localIpAddress = "http://" + ipAddress + ":" + PORT;
            toast("Server started at: " + localIpAddress);
            LogControl.d(TAG, "startServer: ipAddress=" + ipAddress);
            mTextView.setText("浏览器输入: " + localIpAddress +
                    "\n或者扫描二维码" +
                    "\n即可再相同网络下,下载手机的所有文件;" +
                    (TextUtils.isEmpty(pwdHash)
                            ? ""
                            : "\n已设置访问密码:浏览器需先输入密码才能下载;") +
                    "\n其他设备也可通过页面上的表单向 Download 目录上传文件;" +
                    "\n需要保证当前应用一直在前台!!!");
            mTextView.setKeepScreenOn(true);
            isServerRunning = true;
            mServerButton.setText("停止服务");
            mImageView.setVisibility(View.VISIBLE);
            final Bitmap bitmap = QRCodeGenerator.generateQRCode(localIpAddress, 500, 500);
            mImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            toast("failed to start server");
        }
    }

    private void stopServer() {
        if (fileServer != null) {
            fileServer.stop();
            fileServer = null;
            toast("Server stopped");
            mTextView.setKeepScreenOn(false);
        }
        isServerRunning = false;
        if (mServerButton != null) {
            mServerButton.setText("开启服务");
        }
        if (mImageView != null) {
            mImageView.setVisibility(View.GONE);
        }
    }

    /**
     * 密码行按钮:未设置密码时是"保存密码",已设置时是"清空"。
     * 密码明文保存,方便在页面上回看;只对之后开启的服务生效。
     */
    private void onPasswordButtonClick() {
        SPUtils sp = SPUtils.getInstance();
        if (!TextUtils.isEmpty(sp.getString(SP_KEY_PASSWORD, ""))) {
            // 已有密码:清空
            sp.remove(SP_KEY_PASSWORD);
            toast("已清除访问密码(重新开启服务后生效)");
        } else {
            // 没有密码:把输入框里的内容保存为密码
            String pwd = mPasswordEdit.getText() == null ? "" : mPasswordEdit.getText().toString().trim();
            if (pwd.isEmpty()) {
                toast("请先输入访问密码");
                return;
            }
            sp.put(SP_KEY_PASSWORD, pwd);
            toast("访问密码已保存(重新开启服务后生效)");
        }
        updatePasswordStatus();
    }

    /**
     * 网页端发来文字后,把内容显示到界面上。
     * 内容前面带上收到的时间,便于知道网页端是什么时候发过来的;
     * 时间戳只加在显示文本里,复制时用的仍是纯内容。
     */
    private void updatePushedText(String text) {
        mPushedTextValue = (text == null) ? "" : text;
        if (mPushedText != null) {
            if (mPushedTextValue.isEmpty()) {
                mPushedText.setText(PUSHED_TEXT_EMPTY_HINT);
            } else {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
                mPushedText.setText(time + "\n" + mPushedTextValue);
            }
        }
        toast("收到网页发来的内容");
    }

    /**
     * 复制网页端发来的文字到剪贴板。
     * 复制的是 mPushedTextValue(纯内容),界面显示时加的那个时间戳不会被复制。
     */
    private void copyPushedText() {
        if (TextUtils.isEmpty(mPushedTextValue)) {
            toast("还没有收到网页发来的内容");
            return;
        }
        ClipboardManager cm = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm == null) {
            toast("复制失败");
            return;
        }
        cm.setPrimaryClip(ClipData.newPlainText("local_server_push", mPushedTextValue));
        toast("已复制(不含时间戳)");
    }

    private void updatePasswordStatus() {
        String savedPwd = SPUtils.getInstance().getString(SP_KEY_PASSWORD, "");
        boolean hasPwd = !TextUtils.isEmpty(savedPwd);
        mPasswordEdit.setText(savedPwd);
        mPasswordButton.setText(hasPwd ? "清空" : "保存密码");
        mPasswordStatus.setText(hasPwd
                ? "当前访问密码: " + savedPwd + " (重新开启服务后生效)"
                : "当前未设置访问密码:下载无需密码");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopServer();
    }

    private String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4) return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop IPv6 zone suffix
                                return delim < 0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

}
