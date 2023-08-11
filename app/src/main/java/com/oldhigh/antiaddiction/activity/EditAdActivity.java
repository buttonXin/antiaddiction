package com.oldhigh.antiaddiction.activity;

import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.oldhigh.antiaddiction.DataManager;
import com.oldhigh.antiaddiction.R;

import java.util.Set;

public class EditAdActivity extends BaseActivity {

    private Button btnSave;
    private EditText mEditText;
    private TextView mTextView;

    @Override
    protected int layoutId() {
        return R.layout.activity_edit_ad;
    }

    @Override
    protected void initView() {
        mEditText = findViewById(R.id.et_ad);
        mTextView = findViewById(R.id.tv_all_ad);
        btnSave = findViewById(R.id.btn_save);
        findViewById(R.id.btn_delete_all).setOnClickListener(v -> {
            DataManager.get().removeAllEditAd();
            ToastUtils.show("删除所有成功");
            mEditText.getText().clear();
            updateAllAd();
        });

        findViewById(R.id.btn_delete).setOnClickListener(v -> {
            String adName = mEditText.getText().toString();
            if (TextUtils.isEmpty(adName)) {
                ToastUtils.show("请输入广告文案");
                return;
            }
            DataManager.get().removeSkipAd(adName);
            ToastUtils.show("删除成功");
            updateAllAd();
        });

        btnSave.setOnClickListener(v -> {
            String adName = mEditText.getText().toString();
            if (TextUtils.isEmpty(adName)) {
                ToastUtils.show("请输入广告文案");
                return;
            }
            DataManager.get().addSkipAd(adName);
            ToastUtils.show("保存成功");
            mEditText.getText().clear();
            updateAllAd();
        });
    }

    @Override
    protected void initData() {

        updateAllAd();

    }

    public void updateAllAd() {
        Set<String> allAdName = DataManager.get().getAllAdName();

        mTextView.setText(allAdName.toString());
    }
}