package com.xreal.evapro.toolsapp.camrea;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import com.xreal.evapro.toolsapp.util.LogControl;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;

import java.io.IOException;

public class CameraPreviewFG extends BaseOLFragment {

    private final int mCameraId;

    public CameraPreviewFG(int cameraId) {
        mCameraId = cameraId;
    }

    private static final String TAG = CameraPreviewFG.class.getSimpleName();
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("");
    }

    @Override
    public void initData() {

        View decorView = mActivity.getWindow().getDecorView();
        // Hide the status bar.
        // Hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        surfaceView = new SurfaceView(mActivity);
        surfaceView.setKeepScreenOn(true);
        surfaceHolder = surfaceView.getHolder();
        addFullscreenView(surfaceView);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                openCamera();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // 配置预览
                if (camera != null) {
                    try {
                        camera.setPreviewDisplay(holder);
                        camera.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
// Surface 销毁时释放相机
                releaseCamera();
            }
        });
    }

    private void openCamera() {
        if (mActivity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 101);
            return;
        }


        try {
            final int numberOfCameras = Camera.getNumberOfCameras();
            LogControl.d(TAG, "openCamera: numberOfCameras=" + numberOfCameras);

            camera = Camera.open(mCameraId); // 打开后置摄像头
            Camera.Parameters parameters = camera.getParameters();

            // 设置预览分辨率（可以调整为设备支持的分辨率）
            parameters.setPreviewSize(1920, 1080);
            camera.setParameters(parameters);

            // 设置相机旋转角度
            camera.setDisplayOrientation(90); // 根据需要调整旋转角度

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
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }
}
