package com.oldhigh.antiaddiction.feature;

import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oldhigh.antiaddiction.action.MiGuAction;
import com.oldhigh.antiaddiction.action.ShoppingAction;
import com.oldhigh.antiaddiction.activity.BaseOLFragment;
import com.oldhigh.antiaddiction.bean.EventClick;
import com.oldhigh.antiaddiction.util.FloatPointHelper;
import com.oldhigh.antiaddiction.util.SPUtils;
import com.ven.assists.stepper.StepManager;

import java.util.List;

public class AddPointFg extends BaseOLFragment {


    private TextView mTextView;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("记录自动化");
    }

    @Override
    public void initData() {
        addText("点击添加后, 会显示悬浮窗\n" +
                "然后回车,打开对应的应用,点击开始,将点击应用功能的位置后,会显示一个红点\n" +
                "点击保存后,就是自动化的第一步执行,然后 点击停止;\n" +
                "再打开应用的功能位置的下一页, 再次开始, 点击下一次要操作的位置. 依次进行.\n" +
                "最后点击结束.");
        addButton("添加积分", view -> {
            final FloatPointHelper floatPointHelper = new FloatPointHelper(mActivity);
            floatPointHelper.show();
        });
        addButton("查看记录点", view -> {

            final String string = SPUtils.getInstance().getString(SpKey.KEY_POINT);
            final List<EventClick> eventClicks;
            eventClicks = new Gson().fromJson(string, new TypeToken<List<EventClick>>() {
            }.getType());

            mTextView.setText(eventClicks.toString());
        });
        mTextView = addText("");

        addButton("执行", view -> {
            StepManager.INSTANCE.execute(ShoppingAction.class, 1, 0, null, true);
        });
    }
}
