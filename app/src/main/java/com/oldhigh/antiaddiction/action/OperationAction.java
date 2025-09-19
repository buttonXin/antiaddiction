package com.oldhigh.antiaddiction.action;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oldhigh.antiaddiction.App;
import com.oldhigh.antiaddiction.bean.EventClick;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.oldhigh.antiaddiction.util.LogControl;
import com.oldhigh.antiaddiction.util.NotificationHelper;
import com.oldhigh.antiaddiction.util.SPUtils;
import com.ven.assists.service.AssistsService;
import com.ven.assists.stepper.Step;
import com.ven.assists.stepper.StepCollector;
import com.ven.assists.stepper.StepImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 商店打卡-tao bao
 */
public class OperationAction extends StepImpl {
    private static final String TAG = OperationAction.class.getSimpleName();

    private static final int delay = 3000;

    private Map<Integer, EventClick> sActionMap = new HashMap<>();
    private String mNextOperation;

    private android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

    @Override
    public void onImpl(@NonNull StepCollector stepCollector) {


        stepCollector.next(1, true, (step, continuation) -> {
            final String data = (String) step.getData();
            LogControl.d(" data = " + data);

            mNextOperation = "";
            final String string = SPUtils.getInstance().getString(data);
            LogControl.d("string:" + string);
            if (TextUtils.isEmpty(string)) {
                return Step.Companion.getNone();
            }
            final List<EventClick> eventClicks;
            eventClicks = new Gson().fromJson(string, new TypeToken<List<EventClick>>() {
            }.getType());
            if (eventClicks != null) {
                String pkgName = eventClicks.get(0).pkgName;
                for (int i = 0; i < eventClicks.size(); i++) {
                    final EventClick eventClick = eventClicks.get(i);
                    if (i == 0) {
                        continue;
                    }
                    if (!TextUtils.isEmpty(eventClick.nextOperation)) {
                        mNextOperation = eventClick.nextOperation;
                    } else {
                        sActionMap.put(i + 1, eventClick);
                    }
                }
                pointOption(stepCollector);
                openPkgApp(pkgName);
            }

            return Step.Companion.get(2, OperationAction.class, null, delay);
        });
    }

    private void pointOption(StepCollector stepCollector) {
        final Set<Integer> keySet = sActionMap.keySet();
        for (Integer integer : keySet) {
            stepCollector.next(integer, true, (step, continuation) -> {
                final EventClick eventClick1 = sActionMap.get(step.getStep());
                Log.e(TAG, " key =" + step.getStep() + "  " + eventClick1);
                clickByNode(eventClick1.point);
                return Step.Companion.get(step.getStep() + 1, OperationAction.class, null, delay);
            });
        }
        stepCollector.next(sActionMap.size() + 2, true, (step, continuation) -> {
            Log.e(TAG, "onImpl:  step=" + step + "  mNextOperation= " + mNextOperation);

            OtherAction.back();
            handler.postDelayed(OtherAction::back, 500);
            deleteUri();

            if (!TextUtils.isEmpty(mNextOperation)) {
                handler.postDelayed(() -> NotificationHelper.sendNotification(AssistsService.Companion.getInstance().getApplicationContext(), mNextOperation), 2000);
            }
            return Step.Companion.getNone();
        });
    }

    /**
     * 删除保存的uri图片
     */
    private void deleteUri() {
        final String picUri = SPUtils.getInstance().getString(SpKey.screenshot_uri);
        LogControl.d(" picUri:" + picUri);
        ContentResolver contentResolver = App.getInstance().getContentResolver();

        // 使用 ContentResolver 删除 MediaStore 中的条目
        int rowsAffected = contentResolver.delete(Uri.parse(picUri), null, null);
        LogControl.d("delete", rowsAffected);

    }

    private void openPkgApp(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return;
        }
        final Context application = AssistsService.Companion.getInstance().getApplicationContext();
        final Intent intent = application.getPackageManager().getLaunchIntentForPackage(pkgName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }

    public static boolean clickByNode(Point point) {

        GestureDescription.Builder builder = new GestureDescription.Builder();
        Path path = new Path();
        path.moveTo(point.x, point.y);
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0L, 100L));
        GestureDescription gesture = builder.build();


        boolean isDispatched = AssistsService.Companion.getInstance().dispatchGesture(gesture, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
//                LogUtil.d(TAG, "dispatchGesture onCompleted: 完成...");
                Log.e(TAG, "dispatchGesture onCompleted: 完成...");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
//                LogUtil.d(TAG, "dispatchGesture onCancelled: 取消...");
                Log.e(TAG, "dispatchGesture onCancelled: 取消...");
            }
        }, null);

        return isDispatched;
    }
}
