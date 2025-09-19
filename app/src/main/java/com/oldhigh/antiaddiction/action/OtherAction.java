package com.oldhigh.antiaddiction.action;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.oldhigh.antiaddiction.util.FileUtil;
import com.oldhigh.antiaddiction.util.LogControl;
import com.ven.assists.AssistsCore;

import java.util.List;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

/**
 * 其他操作
 */
public class OtherAction {

    private static android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());


    /**
     * 截图
     */
    public static void screenshot() {
        AssistsCore.INSTANCE.takeScreenshot(new Continuation<Bitmap>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NonNull Object result) {
                LogControl.d(" 截图成功");
                // 在这里处理截图结果
                if (result instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) result;
                    new Thread(() -> {
                        String imagePath = FileUtil.saveBitmapToFile(bitmap);
                        LogControl.d("保存图片成功: " + imagePath);
                        if (imagePath != null) {
                            // 在主线程分享
                            FileUtil.shareImageToWeChat(imagePath);
                        }
                        // 释放 Bitmap 资源
                        bitmap.recycle();
                    }).start();
                } else if (result instanceof Throwable) {
                    // 处理错误
                    Throwable error = (Throwable) result;
                    LogControl.e("截图失败: " + error.getMessage());
                }
            }
        });
    }

    /**
     * 返回
     */
    public static void back() {
        AssistsCore.INSTANCE.back();
    }

    /**
     * 清除所有应用
     */
    public static void clearAll() {
        AssistsCore.INSTANCE.recentApps();
        handler.postDelayed(() -> {
            // 可行
            String[] clearAllTexts = {"清理所有", "清除", "全部清除", "关闭所有", "全部关闭", "Clear all", "Close all"};
            for (String clearAllText : clearAllTexts) {
                final List<AccessibilityNodeInfo> nodes = AssistsCore.INSTANCE.findByText(clearAllText, null, null, null);
                if (!nodes.isEmpty()) {
                    for (AccessibilityNodeInfo node : nodes) {
                        if (node.isEnabled() && node.isClickable()) {
                            // 执行点击操作
                            boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            if (clicked) {
                                // 点击后延迟一段时间，然后标记为完成
                                handler.postDelayed(() -> {
                                    LogControl.d(" 点击成功");
                                }, 1000);
                            }
                        }
                    }
                }
            }

//            // 可行 ,但是需要找到对应的id
//            AssistsCore.INSTANCE.findById("com.android.launcher:id/btn_clear"
//                    , null, null, null).get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);

        }, 1300);

    }

    public static void goHome() {
        AssistsCore.INSTANCE.home();
    }

    public static void byText(String text) {
        AssistsCore.INSTANCE.findByText(text, null, null, null).get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }

    public static void byPoint(Point point) {
        AssistsCore.INSTANCE.gestureClick(point.x, point.y, 300, new Continuation<Boolean>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return null;
            }

            @Override
            public void resumeWith(@NonNull Object o) {

            }
        });
    }

    /**
     * 获取所以节点,并找到editText
     */
    public static void byEditTextFirst(String inputText) {
        final List<AccessibilityNodeInfo> allNodes = AssistsCore.INSTANCE.getAllNodes(null, null, null, null);
        inputEditText(inputText, allNodes);
    }

    /**
     * 通过hint 查找id
     * 向editText 输入内容
     */
    public static void byEditText(String hintText, String inputText) {
        final List<AccessibilityNodeInfo> nodeInfos = AssistsCore.INSTANCE.findByText(hintText, null, null, null);
        inputEditText(inputText, nodeInfos);
    }

    private static void inputEditText(String inputText, List<AccessibilityNodeInfo> nodeInfos) {
        for (AccessibilityNodeInfo node : nodeInfos) {
            if (isEditText(node) && node.isEnabled() && node.isVisibleToUser()) {
                inputTextToEditText(node, inputText);
                node.recycle();
                break;
            }
        }
    }

    // 判断节点是否为 EditText
    private static boolean isEditText(AccessibilityNodeInfo node) {
        if (node == null) return false;
        String className = node.getClassName().toString();
        return "android.widget.EditText".equals(className)
                || "androidx.appcompat.widget.AppCompatEditText".equals(className);
    }

    private static void inputTextToEditText(AccessibilityNodeInfo editTextNode, String text) {
        if (editTextNode == null || !editTextNode.isEnabled()) return;

        // 创建 Bundle 传递文本
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);

        // 执行输入操作
        boolean success = editTextNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        if (success) {
            Log.d("Accessibility", "输入成功: " + text);
        } else {
            Log.d("Accessibility", "输入失败，可能被应用限制");
        }
    }
}
