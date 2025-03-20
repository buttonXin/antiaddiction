package com.oldhigh.antiaddiction.action;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.oldhigh.antiaddiction.bean.EventClick;
import com.oldhigh.antiaddiction.util.DeviceManagerUtil;
import com.ven.assists.AssistsCore;
import com.ven.assists.service.AssistsService;
import com.ven.assists.stepper.Step;
import com.ven.assists.stepper.StepCollector;
import com.ven.assists.stepper.StepImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DingDingAction extends StepImpl {
    private static final String TAG = DingDingAction.class.getSimpleName();

    private static final int delay = 5000;

    private static Map<Integer, EventClick> sActionMap = new HashMap<>();

    static {
        sActionMap.put(3, new EventClick("工作台"));
        sActionMap.put(4, new EventClick("考勤打卡", new Point(116, 1546)));
        sActionMap.put(5, new EventClick("打卡"));
        sActionMap.put(6, new EventClick("上班打卡", new Point(560, 1346)));
    }

    @Override
    public void onImpl(@NonNull StepCollector stepCollector) {

        stepCollector.next(1, true, (step, continuation) -> {

            Log.e(TAG, "onImpl: 1 ");

            DeviceManagerUtil.unlockNow();

            return Step.Companion.get(2, DingDingAction.class, null, delay);
        }).next(2, true, (step, continuation) -> {
            Log.e(TAG, "onImpl: 2 ");
            final Context application = AssistsService.Companion.getInstance().getApplicationContext();
            final Intent intent = application.getPackageManager().getLaunchIntentForPackage("com.alibaba.android.rimet");
            application.startActivity(intent);

            return Step.Companion.get(3, DingDingAction.class, null, delay);
        })/*.next(3, true, (step, continuation) -> {
//            final String uiText = sActionMap.get(key);
//            Log.e(TAG, " key =" + key + " uiText =" + uiText);
            AssistsCore.INSTANCE.findByTextAllMatch("工作台").forEach(node -> {
                node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            });
            return Step.Companion.get(4, DingDingAction.class, null, delay);
        })*/;

        // 轮询事件
        final Set<Integer> keySet = sActionMap.keySet();
        for (Integer integer : keySet) {
            stepCollector.next(integer, true, (step, continuation) -> {
                final EventClick eventClick = sActionMap.get(integer);
                Log.e(TAG, " key =" + integer + "  " + eventClick);

                if (eventClick.point == null) {
                    AssistsCore.INSTANCE.findByText(eventClick.clickName).forEach(node -> {
                        node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        clickByNode(node);
                    });
                } else {
                    clickByNode(eventClick.point);
                }

                return Step.Companion.get(integer + 1, DingDingAction.class, null, delay);
            });
        }

        stepCollector.next(7, true, (step, continuation) -> {
            Log.e(TAG, "onImpl: lockNow ");
            DeviceManagerUtil.lockNow();
            return Step.Companion.getNone();
        });
    }

    /**
     * 实现位置坐标点击
     *
     * @return
     */
    public static boolean clickByNode(AccessibilityNodeInfo nodeInfo) {

        Rect rect = new Rect();
        nodeInfo.getBoundsInScreen(rect);
        int x = rect.centerX();
        int y = rect.centerY();
        Log.e("acc_", "要点击的像素点在手机屏幕位置::" + rect.centerX() + " " + rect.centerY());
        Point point = new Point(x, y);

        return clickByNode(point);
    }

    public static boolean clickByNode(Point point) {

//        Rect rect = new Rect();
//        nodeInfo.getBoundsInScreen(rect);
//        int x = rect.centerX();
//        int y = rect.centerY();
//        Log.e("acc_", "要点击的像素点在手机屏幕位置::" + rect.centerX() + " " + rect.centerY());
//        Point point = new Point(x, y);
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
