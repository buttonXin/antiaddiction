package com.xreal.evapro.toolsapp.camrea;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.DensityUtil;
import com.xreal.evapro.toolsapp.util.LogControl;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 查看IT之家
 */
public class WebITHomeFG extends BaseOLFragment {

    private int mCameraId;
    private LinearLayout mLlTV;
    private WebView mWebView;

    public WebITHomeFG() {
    }

    private static final String TAG = WebITHomeFG.class.getSimpleName();
    private SurfaceTexture surfaceTexture;
    private Camera camera;

    private static final String itURL = "https://m.ithome.com/";

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("IT之家");
    }

    @Override
    public void initData() {

        mCameraId = Integer.parseInt(getContent());

        initWebView();


        addBottomTV("1", v -> openCamera());
        addBottomTV("2", v -> takePicture());
//        addBottomTV("3", v -> releaseCamera());
        addBottomTV("4", v -> {
            releaseCamera();
            new CacheImgFG().openFragment(getFragmentManager());
        });

    }

    private void addBottomTV(String text, View.OnClickListener listener) {
        final TextView view = new TextView(mActivity);
        if (mLlTV == null) {
            mLlTV = new LinearLayout(mActivity);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM | Gravity.END;
            params.bottomMargin = DensityUtil.dip2px(160);
            params.rightMargin = DensityUtil.dip2px(20);
            mFrameLayout.addView(mLlTV, params);
            mLlTV.setOrientation(LinearLayout.VERTICAL);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                DensityUtil.dip2px(50), DensityUtil.dip2px(30));
        params.bottomMargin = DensityUtil.dip2px(30);
        mLlTV.addView(view, params);
        view.setText(text);
        view.setTextColor(Color.BLACK);
        view.setGravity(Gravity.CENTER);
        view.setOnClickListener(v -> {
            listener.onClick(v);
            view.setTextColor(Color.WHITE);
            handler.postDelayed(() -> view.setTextColor(Color.BLACK), 200);
        });

    }

    public WebView getWebView() {
        return mWebView;
    }

    private void initWebView() {
        mWebView = new WebView(mActivity);
        addFullscreenView(mWebView);
        mWebView.setBackgroundColor(Color.BLACK);
//声明WebSettings子类
        WebSettings webSettings = mWebView.getSettings();

        WebView.setWebContentsDebuggingEnabled(true);
//设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
//缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

//其他细节操作
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT); //关闭webview中缓存
        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式
        webSettings.setDomStorageEnabled(true);

//
//        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
//        webView.setScrollbarFadingEnabled(true);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

//        webView.setInitialScale(30);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        // 开启混合加载 设置加载不安全资源的WebView加载行为
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

//        webSettings.setUserAgentString("PC");
        mWebView.loadUrl(itURL);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

            }

            //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }

        });
    }

    private void releaseWebView() {
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
            mWebView.setVisibility(View.GONE);
            mWebView = null;
        }
    }

    private void openCamera() {
        final int cameraPermission = mActivity.checkSelfPermission(Manifest.permission.CAMERA);
        LogControl.d("cameraPermission=", cameraPermission);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }

        if (camera != null) {
            releaseCamera();
            return;
        }

        try {
            final int numberOfCameras = Camera.getNumberOfCameras();
            LogControl.d(TAG, "openCamera: numberOfCameras=" + numberOfCameras);

            camera = Camera.open(mCameraId); // 打开后置摄像头
            surfaceTexture = new SurfaceTexture(1024);
            final Camera.Parameters parameters = camera.getParameters();
//            parameters.setPreviewSize(1920, 1080);
            parameters.setRotation(90);
            camera.setParameters(parameters);
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();


        } catch (Exception e) {
            toast("无法打开相机");
            e.printStackTrace();
        }
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        if (surfaceTexture != null) {
            surfaceTexture.release();
            surfaceTexture = null;
        }
    }

    private void takePicture() {
        LogControl.d("camera", camera);
        if (camera != null) {

            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    saveImageToCache(data);
                    camera.startPreview(); // 拍照后继续预览
                }
            });
        }
    }

    private void saveImageToCache(byte[] data) {
        try {

            long start = System.currentTimeMillis();
            LogControl.d("start saveImageToCache", start);
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                LogControl.d(TAG, "Error creating media file, check storage permissions.");
                return;
            }
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            LogControl.d("end saveImageToCache", System.currentTimeMillis() - start);


        } catch (Exception e) {
            toast("failed");
        }

    }


    private File getOutputMediaFile() {
        File mediaStorageDir = new File(mActivity.getExternalCacheDir(), "my_camera_file");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
    }


    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    public void closeFragment() {
        releaseWebView();
        super.closeFragment();
    }
}
