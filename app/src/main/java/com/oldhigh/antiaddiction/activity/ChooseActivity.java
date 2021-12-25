package com.oldhigh.antiaddiction.activity;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.oldhigh.antiaddiction.DataManager;
import com.oldhigh.antiaddiction.R;
import com.oldhigh.antiaddiction.adapter.CustomAdapter;
import com.oldhigh.antiaddiction.bean.AppInfo;
import com.oldhigh.antiaddiction.util.HelpUtil;

import java.util.List;

public class ChooseActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button btnSave;

    @Override
    protected int layoutId() {
        return R.layout.activity_choose;
    }

    @Override
    protected void initView() {
        recyclerView = findViewById(R.id.rv);
        progressBar = findViewById(R.id.pb);
        btnSave = findViewById(R.id.btn_save);
        GridLayoutManager layout = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layout);

        btnSave.setOnClickListener(v -> {
            DataManager.get().saveAll();
            ToastUtils.show("保存成功");
            finish();
        });
    }

    @Override
    protected void initData() {


        new Thread(() -> {
            List<AppInfo> packages = HelpUtil.getPackages(getApplicationContext());
            runOnUiThread(() -> {
                CustomAdapter customAdapter = new CustomAdapter(packages, true);
                recyclerView.setAdapter(customAdapter);
                progressBar.setVisibility(View.GONE);
            });
        }).start();


    }
}