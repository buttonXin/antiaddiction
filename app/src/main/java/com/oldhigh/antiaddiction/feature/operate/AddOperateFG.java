package com.oldhigh.antiaddiction.feature.operate;

import android.content.Intent;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oldhigh.antiaddiction.activity.BaseOLFragment;
import com.oldhigh.antiaddiction.bean.EventClick;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.oldhigh.antiaddiction.feature.other_3d_app.SelectAppFragment;
import com.oldhigh.antiaddiction.util.LogControl;
import com.oldhigh.antiaddiction.util.SPUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddOperateFG extends BaseOLFragment {


    private TextView mTextView;
    private EditText mEditTextAppTime;
    private String mPackageName;
    private Button mSelectApp;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("新增操作组");
    }

    private EditText mEditText;

    @Override
    public void initData() {
        addText("点击添加后, 会显示悬浮窗\n" +
                "然后回车,打开对应的应用,点击开始,将点击应用功能的位置后,会显示一个红点\n" +
                "点击保存后,就是自动化的第一步执行,然后 点击停止;\n" +
                "再打开应用的功能位置的下一页, 再次开始, 点击下一次要操作的位置. 依次进行.\n" +
                "最后点击结束.");
        addLine();
        mEditText = addEditText("请先输入操作组名称");
        mEditTextAppTime = addEditText("启动app后多少秒执行", 1);
        mEditTextAppTime.setInputType(InputType.TYPE_CLASS_NUMBER);
        mSelectApp = addButton("选择启动的app", 1, v -> {
            final String content = mEditText.getText().toString().trim();
            final String time = mEditTextAppTime.getText().toString();
            LogControl.d("content:" + content);
            if (TextUtils.isEmpty(content) || TextUtils.isEmpty(time)) {
                toast("请输入内容 / 时间");
                return;
            }
            final SelectAppFragment selectAppFragment = new SelectAppFragment();
            getFragmentManager().beginTransaction()
                    .add(android.R.id.content, selectAppFragment)
                    .addToBackStack(selectAppFragment.getClass().getSimpleName())
                    .commit();
            selectAppFragment.setOnAppItemClickListener(packageInfo -> {
                LogControl.d(" packageName:" + packageInfo.packageName);

                String string = SPUtils.getInstance().getString(content);
                final List<EventClick> eventClicks;
                if (TextUtils.isEmpty(string)) {
                    eventClicks = new ArrayList<>();
                } else {
                    // [{xxx,xxx},{xxx,xxx}]
                    eventClicks = new Gson().fromJson(string, new TypeToken<List<EventClick>>() {
                    }.getType());
                }
                final EventClick eventClick = new EventClick();
                mPackageName = packageInfo.packageName;
                eventClick.pkgName = packageInfo.packageName;
                eventClick.delayTime = Integer.parseInt(time);
                eventClick.nickName = packageInfo.applicationInfo.loadLabel(mActivity.getPackageManager()).toString();
                mSelectApp.setText(eventClick.nickName);
                eventClicks.add(eventClick);

                SPUtils.getInstance().put(content, new Gson().toJson(eventClicks));
            });
        });
        addButton("启动悬浮窗", v -> {
            final String content = mEditText.getText().toString();
            LogControl.d("content:" + content);
            if (TextUtils.isEmpty(content)) {
                toast("请输入内容");
                return;
            }
            if (TextUtils.isEmpty(mPackageName)) {
                toast("请先选择启动的app");
                return;
            }
            final FloatPointHelper floatPointHelper = new FloatPointHelper(mActivity);
            floatPointHelper.show(content);

            final Set<String> stringSet = SPUtils.getInstance().getStringSet(SpKey.KEY_POINT_list);
            final HashSet<String> endSets = new HashSet<>();
            endSets.add(content);
            if (stringSet != null) {
                endSets.addAll(stringSet);
            }
            SPUtils.getInstance().put(SpKey.KEY_POINT_list, endSets);

            final Intent intent = mActivity.getPackageManager().getLaunchIntentForPackage(mPackageName);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(intent);
        });
        addLine();
        addButton("查看操作组", v -> checkPoint());
        addButton("删除操作组", v -> {
            final String content = mEditText.getText().toString();
            SPUtils.getInstance().remove(content);
        });
        addLine();

        mTextView = addText("");

    }

    private void checkPoint() {
        final String mContent = mEditText.getText().toString();
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

}
