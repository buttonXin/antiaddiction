package com.oldhigh.antiaddiction.feature.operate;

import com.oldhigh.antiaddiction.activity.BaseOLFragment;
import com.oldhigh.antiaddiction.feature.SpKey;
import com.oldhigh.antiaddiction.util.SPUtils;

import java.util.Set;

public class PointFg extends BaseOLFragment {

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("操作组");
    }

    @Override
    public void initData() {

        addButton("添加新的操作组", view -> {
            new AddOperateFG().openFragment(getFragmentManager());
        });

        addLine();
        addText("下面是已经添加的自动化操作");
        addLine();
        final Set<String> stringSet = SPUtils.getInstance().getStringSet(SpKey.KEY_POINT_list);
        for (String s : stringSet) {
            addButton(s, v -> {
                new SinglePointFG().setBaseParams(s).openFragment(getFragmentManager());
            });
        }

    }
}
