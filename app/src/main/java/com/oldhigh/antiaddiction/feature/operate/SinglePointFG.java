package com.oldhigh.antiaddiction.feature.operate;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oldhigh.antiaddiction.action.OperationAction;
import com.oldhigh.antiaddiction.activity.BaseOLFragment;
import com.oldhigh.antiaddiction.bean.EventClick;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.oldhigh.antiaddiction.util.LogControl;
import com.oldhigh.antiaddiction.util.SPUtils;
import com.ven.assists.stepper.StepManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SinglePointFG extends BaseOLFragment {


    private String mContent;

    @Override
    protected void addTitleBar(String text) {
        mContent = getContent();

        super.addTitleBar(mContent);
    }


    @Override
    public void initData() {

        addButton("执行操作组", v -> {
            StepManager.INSTANCE.execute(OperationAction.class, 1, 100, mContent, true);
        });
        addButton("删除操作组", v -> {
            SPUtils.getInstance().remove(mContent);
            final Set<String> stringSet = SPUtils.getInstance().getStringSet(SpKey.KEY_POINT_list);
            final HashSet<String> strings = new HashSet<>();
            if (stringSet != null) {
                strings.addAll(stringSet);
            }
            strings.remove(mContent);
            SPUtils.getInstance().put(SpKey.KEY_POINT_list, strings);
        });
        addLine();

        final String string = SPUtils.getInstance().getString(mContent);
        LogControl.d("string:" + string);
        if (TextUtils.isEmpty(string)) {
            return;
        }
        final List<EventClick> eventClicks;
        eventClicks = new Gson().fromJson(string, new TypeToken<List<EventClick>>() {
        }.getType());
        if (eventClicks != null) {
            for (int i = 0; i < eventClicks.size(); i++) {
                addText(eventClicks.get(i).point + "  pkg=" + eventClicks.get(i).pkgName);
            }
        }
    }

}
