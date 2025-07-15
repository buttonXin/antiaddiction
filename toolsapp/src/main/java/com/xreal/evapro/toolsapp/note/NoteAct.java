package com.xreal.evapro.toolsapp.note;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.xreal.evapro.toolsapp.MainActivity;
import com.xreal.evapro.toolsapp.R;
import com.xreal.evapro.toolsapp.base.BaseOLActivity;
import com.xreal.evapro.toolsapp.camrea.WebITHomeFG;
import com.xreal.evapro.toolsapp.util.LogControl;
import com.xreal.evapro.toolsapp.util.NotificationHelper;
import com.xreal.evapro.toolsapp.util.SPUtils;

public class NoteAct extends BaseOLActivity {

    public static final String KEY_NOTE_CONTENT = "KEY_NOTE_CONTENT";

    private EditText mEditText;


    @Override
    protected void addTitleBar(String text, View.OnClickListener listener) {
        super.addTitleBar("记录", v -> startAct(MainActivity.class));
    }

    @Override
    public void initData() {
        addButton("更新通知", 1, v -> {
            NotificationHelper.getInstance().show();
            // 隐藏输入法
            hideInputMethod();
        });
        addButton("停止通知", 1, v -> {
            NotificationHelper.getInstance().hide();
            hideInputMethod();
        });
        addButton("IT之家", 1, v -> new WebITHomeFG().setBaseParams("0").openFragment(getFragmentManager()));

        final String noteContent = SPUtils.getInstance().getString(KEY_NOTE_CONTENT);

        mEditText = addEditText("输入内容");
        mEditText.setHint("输入内容");
        mEditText.setLines(5);
        mEditText.setTextColor(Color.BLACK);
        mEditText.setBackgroundColor(Color.TRANSPARENT);
        mEditText.setTextCursorDrawable(R.drawable.edit_text_cursor);
        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);


        if (!TextUtils.isEmpty(noteContent)) {
            mEditText.setText(noteContent);
        }
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                SPUtils.getInstance().put(KEY_NOTE_CONTENT, s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // 监听键盘的完成事件
        mEditText.setOnEditorActionListener((v, actionId, event) -> {
            LogControl.d("actionId:" + actionId);
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == 0) {

                final String string = mEditText.getText().toString();
                LogControl.d("string:" + string);
                SPUtils.getInstance().put(KEY_NOTE_CONTENT, string.trim());
                NotificationHelper.getInstance().show();
                hideInputMethod();
                return false;
            }
            return false;
        });


    }

    private void hideInputMethod() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        }
    }
}
