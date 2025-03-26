package com.xreal.evapro.toolsapp.util;



import android.util.Log;

import java.util.Arrays;

public class LogControl {

    private static String TAG = "OL_tools";

    public static void setBaseLogTag(String tag) {
        TAG = tag;
    }

    public static void d(Object... msg) {
        String log = createLog(Arrays.toString(msg));
        Log.d(TAG, log);
    }

    public static void i(Object... msg) {
        String log = createLog(Arrays.toString(msg));
        LogControl.d(TAG, log);
    }


    public static void v(Object... msg) {
        String log = createLog(Arrays.toString(msg));
        LogControl.d(TAG, log);
    }

    public static void w(Object... msg) {
        String log = createLog(Arrays.toString(msg));
        Log.w(TAG, log);
    }

    public static void e(Object... msg) {
        String log = createLog(Arrays.toString(msg));
        LogControl.d(TAG, log);
    }

    private static String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }

    private static int getStackOffset(StackTraceElement[] trace) {
        for (int i = 2; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (!name.equals(LogControl.class.getName())) {
                return --i;
            }
        }
        return -1;
    }

    private static String createLog(Object stringLog) {
        StackTraceElement[] trace = Thread.currentThread().getStackTrace();

        int methodCount = 1;
        int stackOffset = getStackOffset(trace);

        if (methodCount + stackOffset > trace.length) {
            methodCount = trace.length - stackOffset - 1;
        }

        for (int i = methodCount; i > 0; i--) {
            int stackIndex = i + stackOffset;
            if (stackIndex >= trace.length) {
                continue;
            }
            StackTraceElement element = trace[stackIndex];

            return "(" + element.getFileName() + ":" + element.getLineNumber() + ")" + "#" + element.getMethodName() +
                    " | " + stringLog;
        }
        return "";
    }

}
