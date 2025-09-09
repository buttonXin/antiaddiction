package com.oldhigh.antiaddiction.action;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.oldhigh.antiaddiction.bean.EventClick;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.oldhigh.antiaddiction.util.LogControl;
import com.oldhigh.antiaddiction.util.SPUtils;
import com.ven.assists.service.AssistsService;
import com.ven.assists.stepper.Step;
import com.ven.assists.stepper.StepCollector;
import com.ven.assists.stepper.StepImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 商店打卡-tao bao
 */
public class ShoppingAction extends StepImpl {
    private static final String TAG = DingDingAction.class.getSimpleName();

    private static final int delay = 3000;

    private Map<Integer, EventClick> sActionMap = new HashMap<>();


    public ShoppingAction() {
        final String string = SPUtils.getInstance().getString(SpKey.KEY_POINT);
        LogControl.d("string = " + string);
        final String[] split = string.split("\n");
        for (int i = 0; i < split.length; i++) {
            final String s = split[i];
            if(TextUtils.isEmpty(s.trim())){
                continue;
            }
            final String[] split1 = s.split("-");
            final int x = Integer.parseInt(split1[0]);
            final int y = Integer.parseInt(split1[1]);
            sActionMap.put(i + 2, new EventClick("", new Point(x, y)));
        }
    }

    @Override
    public void onImpl(@NonNull StepCollector stepCollector) {


        stepCollector.next(1, true, (step, continuation) -> {

            final Context application = AssistsService.Companion.getInstance().getApplicationContext();
//            final Intent intent = application.getPackageManager().getLaunchIntentForPackage("com.taobao.taobao");
//            final Intent intent = application.getPackageManager().getLaunchIntentForPackage("com.xreal.evapro.nebula");
            final Intent intent = application.getPackageManager().getLaunchIntentForPackage("tv.danmaku.bili");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(intent);

            return Step.Companion.get(2, this.getClass(), null, delay);
        });

        final Set<Integer> keySet = sActionMap.keySet();
        for (Integer integer : keySet) {
            stepCollector.next(integer, true, (step, continuation) -> {
                final EventClick eventClick = sActionMap.get(step.getStep());
                Log.e(TAG, " key =" + step.getStep() + "  " + eventClick);
                clickByNode(eventClick.point);


                return Step.Companion.get(step.getStep() + 1, ShoppingAction.class, null, delay);
            });
        }
        stepCollector.next(sActionMap.size() + 2, true, (step, continuation) -> {
            Log.e(TAG, "onImpl:  step=" + step);

            return Step.Companion.getNone();
        });
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
