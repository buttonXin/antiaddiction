package com.oldhigh.antiaddiction;

import android.view.View;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.oldhigh.antiaddiction.adapter.CustomAdapter;
import com.oldhigh.antiaddiction.bean.AppInfo;
import com.oldhigh.antiaddiction.util.HelpUtil;

import java.util.List;

public class ChooseActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    @Override
    protected int layoutId() {
        return R.layout.activity_choose;
    }

    @Override
    protected void initView() {
        recyclerView = findViewById(R.id.rv);
        progressBar = findViewById(R.id.pb);

        GridLayoutManager layout = new GridLayoutManager(this,4);
        recyclerView.setLayoutManager(layout);

    }

    @Override
    protected void initData() {


        new Thread(() -> {
            List<AppInfo> packages = HelpUtil.getPackages(getApplicationContext());
            runOnUiThread(() -> {
                CustomAdapter customAdapter = new CustomAdapter(packages);
                recyclerView.setAdapter(customAdapter);
                progressBar.setVisibility(View.GONE);
            });
        }).start();






    }
}