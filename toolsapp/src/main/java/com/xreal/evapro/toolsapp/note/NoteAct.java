package com.xreal.evapro.toolsapp.note;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.xreal.evapro.toolsapp.base.BaseOLActivity;
import com.xreal.evapro.toolsapp.util.LogControl;
import com.xreal.evapro.toolsapp.util.NotificationHelper;
import com.xreal.evapro.toolsapp.util.SPUtils;

public class NoteAct extends BaseOLActivity {

    public static final String KEY_NOTE_CONTENT = "KEY_NOTE_CONTENT";

    private EditText mEditText;

    @Override
    protected void addTitleBar(String text) {
        super.addTitleBar("记录");
    }

    @Override
    public void initData() {
        addButton("启动通知", 1, v -> {
            NotificationHelper.getInstance().show();
        });
        addButton("停止通知", 1, v -> {
            NotificationHelper.getInstance().hide();
        });

        addButton("更新内容", 1, v -> {
            NotificationHelper.getInstance().changeContent(SPUtils.getInstance().getString(KEY_NOTE_CONTENT));
        });
        final String noteContent = SPUtils.getInstance().getString(KEY_NOTE_CONTENT);

        mEditText = addEditText("输入内容");
        mEditText.setHint("输入内容");
        mEditText.setTextColor(Color.BLACK);
        mEditText.setBackgroundColor(Color.TRANSPARENT);

        if (!TextUtils.isEmpty(noteContent)) {
            mEditText.setText(noteContent);
        }
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                SPUtils.getInstance().put(KEY_NOTE_CONTENT, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }
}
