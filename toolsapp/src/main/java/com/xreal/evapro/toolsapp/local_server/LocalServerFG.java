package com.xreal.evapro.toolsapp.local_server;

import android.Manifest;
import android.content.Intent;
import android.provider.Settings;

import com.xreal.evapro.toolsapp.util.LogControl;

import android.widget.TextView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;


public class LocalServerFG extends BaseOLFragment {

    private boolean isOnlyDownload;

    public LocalServerFG(boolean isOnlyDownload) {
        this.isOnlyDownload = isOnlyDownload;
    }

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

        mTextView = addText("");
        addButton("申请权限", v -> {
            // 动态申请权限
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 110);

        });

        addButton("开启服务", 2, v -> {
            startServer();
        });
        addButton("停止服务", 2, v -> {
            stopServer();
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

    @Override
    protected boolean isBlackScreen() {
        return true;
    }


    private void startServer() {
        try {
            fileServer = new FileServer(PORT, getActivity(), isOnlyDownload);
            fileServer.start();
            String ipAddress = getIPAddress(true); // 获取本地IP
            toast("Server started at: http://" + ipAddress + ":" + PORT);
            LogControl.d(TAG, "startServer: ipAddress=" + ipAddress);
            mTextView.setText("浏览器输入: http://" + ipAddress + ":" + PORT +
                    "\n即可再相同网络下,下载手机的所有文件;" +
                    "\n\n需要保证当前应用一直在前台!!!");
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
