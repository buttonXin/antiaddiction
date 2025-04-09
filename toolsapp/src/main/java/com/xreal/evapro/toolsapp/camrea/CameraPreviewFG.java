package com.xreal.evapro.toolsapp.camrea;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.xreal.evapro.toolsapp.base.BaseOLFragment;
import com.xreal.evapro.toolsapp.util.LogControl;

import java.io.IOException;

/**
 * 全屏相机功能
 */
public class CameraPreviewFG extends BaseOLFragment {

    private int mCameraId;

    public CameraPreviewFG() {
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
    protected boolean hasFullScreen() {
        return true;
    }

    @Override
    public void initData() {

        mCameraId = Integer.parseInt(getContent());

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
