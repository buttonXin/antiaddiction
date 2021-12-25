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

import java.util.List;

public class SelectedActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Button btnSave;
    private CustomAdapter customAdapter;

    @Override
    protected int layoutId() {
        return R.layout.activity_choose;
    }

    @Override
    protected void initView() {
        recyclerView = findViewById(R.id.rv);
        progressBar = findViewById(R.id.pb);
        btnSave = findViewById(R.id.btn_save);
        btnSave.setText(R.string.clear);

        GridLayoutManager layout = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(layout);

        btnSave.setOnClickListener(v -> {
            DataManager.get().clear();
            ToastUtils.show("没有了");

            if (customAdapter == null) {
                return;
            }
            customAdapter.updateAll(DataManager.get().getSaveApps());
        });
    }

    @Override
    protected void initData() {

        List<AppInfo> packages = DataManager.get().getSaveApps();
        customAdapter = new CustomAdapter(packages, false);
        recyclerView.setAdapter(customAdapter);
        progressBar.setVisibility(View.GONE);


    }
}