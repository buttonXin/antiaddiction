package com.oldhigh.antiaddiction.feature.operate;

import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

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
    private TextView mTextView;

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
            removeFragment();
        });
        otherActions();

        addLine();

        mTextView = addText("");
        getOperationText();

    }

    private void getOperationText() {
        final String string = SPUtils.getInstance().getString(mContent);
        LogControl.d("string:" + string);
        if (TextUtils.isEmpty(string)) {
            return;
        }
        final List<EventClick> eventClicks;
        eventClicks = new Gson().fromJson(string, new TypeToken<List<EventClick>>() {
        }.getType());
        if (eventClicks != null) {
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < eventClicks.size(); i++) {

                text.append(
                        "第" + i + "步" + " " +
                                (TextUtils.isEmpty(eventClicks.get(i).nickName) ? "" : "名称: " + eventClicks.get(i).nickName)
                                + "  " + (eventClicks.get(i).delayTime == 0 ? "" : eventClicks.get(i).delayTime + "s")
                                + (eventClicks.get(i).point == null ? "" : "  " + eventClicks.get(i).point)
                                + (TextUtils.isEmpty(eventClicks.get(i).pkgName) ? "" : "  pkg=" + eventClicks.get(i).pkgName)
                                + (TextUtils.isEmpty(eventClicks.get(i).nextOperation) ? "" : "  下一步执行:" + eventClicks.get(i).nextOperation)

                );
                text.append("\n");
            }
            mTextView.setText(text.toString());
        }
    }


    private void otherActions() {
        addText("执行结束后,执行其他操作组,填写名称");
        final EditText editText = addEditText("结束后,执行其他操作,", 0);
        addButton("保存", 0, v -> {

            final String otherOperation = editText.getText().toString();
            if (TextUtils.isEmpty(otherOperation)) {
                toast("请填写其他操作组名称");
                return;
            }

            final String string = SPUtils.getInstance().getString(mContent);
            LogControl.d("string:" + string);
            if (TextUtils.isEmpty(string)) {
                return;
            }
            final List<EventClick> eventClicks;
            eventClicks = new Gson().fromJson(string, new TypeToken<List<EventClick>>() {
            }.getType());
            final EventClick eventClick = new EventClick();
            eventClick.nextOperation = otherOperation;

            eventClicks.add(eventClick);
            SPUtils.getInstance().put(mContent, new Gson().toJson(eventClicks));
            handler.postDelayed(this::getOperationText, 200);
        });
    }

}
