package com.oldhigh.antiaddiction.feature.operate;

import com.oldhigh.antiaddiction.IResult;
import com.oldhigh.antiaddiction.activity.BaseOLFragment;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.oldhigh.antiaddiction.util.LogControl;
import com.oldhigh.antiaddiction.util.SPUtils;

import java.util.Set;

public class PointFg extends BaseOLFragment {

    private IResult<Boolean> mIResult = result -> {
        LogControl.d(" 添加结果：" + result);
        removeLLView();
        if (result) {
            showView();
        }
    };


    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("操作组");
    }

    @Override
    public void initData() {

        addButton("添加新的操作组", view -> {

            new AddOperateFG().openFragment(getFragmentManager(), mIResult);
        });

        addLine();
        addText("下面是已经添加的自动化操作");
        addLine();
        showView();

    }

    private void showView() {
        final Set<String> stringSet = SPUtils.getInstance().getStringSet(SpKey.KEY_POINT_list);
        for (String s : stringSet) {
            mLLViews.add(
                    addButton(s, v -> {
                        new SinglePointFG().setBaseParams(s).openFragment(getFragmentManager(), mIResult);
                    }));
        }
    }

}
