package com.xreal.evapro.toolsapp.camrea;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
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
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 查看IT之家
 */
public class WebITHomeFG extends BaseOLFragment {

    private int mCameraId;
    private LinearLayout mLlTV;

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
        addBottomTV("3", v -> releaseCamera());

    }

    private void addBottomTV(String text, View.OnClickListener listener) {
        final TextView view = new TextView(mActivity);
        if (mLlTV == null) {
            mLlTV = new LinearLayout(mActivity);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            mFrameLayout.addView(mLlTV, params);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, DensityUtil.dip2px(30));
        params.weight = 1;
        params.bottomMargin = DensityUtil.dip2px(10);
        mLlTV.addView(view, params);
        view.setText(text);
        view.setGravity(Gravity.CENTER);
        view.setOnClickListener(listener);

    }

    private void initWebView() {
        final WebView webView = new WebView(mActivity);
        addFullscreenView(webView);
        webView.setBackgroundColor(Color.BLACK);
//声明WebSettings子类
        WebSettings webSettings = webView.getSettings();

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
        webView.loadUrl(itURL);
        webView.setWebViewClient(new WebViewClient() {

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

    private void openCamera() {
        final int cameraPermission = mActivity.checkSelfPermission(Manifest.permission.CAMERA);
        final int sdcardPermission = mActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        LogControl.d("cameraPermission=", cameraPermission, "sdcardPermission=", sdcardPermission);
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }


        try {
            final int numberOfCameras = Camera.getNumberOfCameras();
            LogControl.d(TAG, "openCamera: numberOfCameras=" + numberOfCameras);

            camera = Camera.open(mCameraId); // 打开后置摄像头
            surfaceTexture = new SurfaceTexture(1024);
            final Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(1920, 1080);
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
                    saveImage(data);
                    camera.startPreview(); // 拍照后继续预览
                }
            });
        }
    }

    private void saveImage(byte[] data) {
        try {
//            File pictureFile = getOutputMediaFile();
//            if (pictureFile == null) {
//                LogControl.d(TAG, "Error creating media file, check storage permissions.");
//                return;
//            }
//            FileOutputStream fos = new FileOutputStream(pictureFile);
//            fos.write(data);
//            fos.close();

            // 将图片插入 MediaStore，以便在相册中显示
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_" + timeStamp + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
//            values.put(MediaStore.Images.Media.DATA, pictureFile.getAbsolutePath());
            values.put(MediaStore.Images.Media.DATA, data);

            ContentResolver contentResolver = mActivity.getContentResolver();
            Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                try (OutputStream output = contentResolver.openOutputStream(uri)) {
                    output.write(data);
                    toast("success");
                } catch (IOException e) {
                    e.printStackTrace();
                    toast("failed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getOutputMediaFile() {
        File mediaStorageDir = new File(mActivity.getExternalFilesDir(null), "my_camera_file");
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
    }
}
