package com.xreal.evapro.toolsapp.local_server;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Size;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.xreal.evapro.toolsapp.R;
import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.LogControl;
import com.xreal.evapro.toolsapp.util.SPUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;


public class LocalServerFG extends BaseOLFragment {

    private boolean isOnlyDownload;
    private ImageView mImageView;

    // 访问密码设置(明文保存,方便查看;哈希在开启服务时计算并传给服务器校验)
    private static final String SP_KEY_PASSWORD = "local_server_password";
    private TextView mPasswordStatus;
    private EditText mPasswordEdit;

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
        addButton("申请权限", v -> {
            // 动态申请权限
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 110);

        });

        // 访问密码:必须在开启服务前设置好,保存后需重新开启服务才生效
        mPasswordStatus = addText("");
        mPasswordEdit = addEditText("设置访问密码(明文显示,方便查看;留空保存=清除密码;保存后重新开启服务生效)");
        mPasswordEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        addButton("保存密码", v -> savePassword());
        updatePasswordStatus();

        addButton("开启服务", 2, v -> {
            startServer();
            mImageView.setVisibility(View.VISIBLE);
            final Bitmap bitmap = QRCodeGenerator.generateQRCode(localIpAddress, 500, 500);
            mImageView.setImageBitmap(bitmap);
        });
        addButton("停止服务", 2, v -> {
            stopServer();
            mImageView.setVisibility(View.GONE);
            mTextView.setText("服务已经停止");
        });

        addButton("保存内容到-下载", v -> {
            new CopyClipToFileFG().openFragment(getFragmentManager());
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


    private void startServer() {
        try {
            // 密码只在开启服务前设置才生效:启动时读取已保存的明文并计算哈希,运行中改密码需重启服务
            String pwd = SPUtils.getInstance().getString(SP_KEY_PASSWORD, "");
            String pwdHash = TextUtils.isEmpty(pwd) ? "" : FileServer.sha256Hex(pwd);
            fileServer = new FileServer(PORT, getActivity(), isOnlyDownload, pwdHash);
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
        } catch (IOException e) {
            e.printStackTrace();
            toast("failed to start server");
        }
    }

    private void stopServer() {
        if (fileServer != null) {
            fileServer.stop();
            toast("Server stopped");
            mTextView.setKeepScreenOn(false);
        }
    }

    /**
     * 保存访问密码:明文保存,方便在页面上回看;留空保存表示清除密码。
     * 密码只对之后开启的服务生效。
     */
    private void savePassword() {
        String pwd = mPasswordEdit.getText() == null ? "" : mPasswordEdit.getText().toString().trim();
        SPUtils sp = SPUtils.getInstance();
        if (pwd.isEmpty()) {
            sp.remove(SP_KEY_PASSWORD);
            mPasswordEdit.setText("");
            toast("已清除访问密码(重新开启服务后生效)");
        } else {
            sp.put(SP_KEY_PASSWORD, pwd);
            toast("访问密码已保存(重新开启服务后生效)");
        }
        updatePasswordStatus();
    }

    private void updatePasswordStatus() {
        String savedPwd = SPUtils.getInstance().getString(SP_KEY_PASSWORD, "");
        mPasswordEdit.setText(savedPwd);
        mPasswordStatus.setText(TextUtils.isEmpty(savedPwd)
                ? "当前未设置访问密码:下载无需密码"
                : "当前访问密码: " + savedPwd + " (重新开启服务后生效)");
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
