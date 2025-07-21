package com.xreal.evapro.toolsapp.note;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
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
    public static final String KEY_NOTE_CONTENT_2 = "KEY_NOTE_CONTENT_2";



    @Override
    protected void addTitleBar(String text, View.OnClickListener listener) {
        super.addTitleBar("记录", v -> startAct(MainActivity.class));
    }

    @Override
    public void initData() {
        addButton("更新通知", 1, v -> {
            NotificationHelper.getInstance().show();
        });
        addButton("停止通知", 1, v -> {
            NotificationHelper.getInstance().hide();
        });
        addButton("IT之家", 1, v -> new WebITHomeFG().setBaseParams("0").openFragment(getFragmentManager()));


        initNotificationET();
        addLine();
        initNoteET();


    }


    private void initNotificationET() {
        EditText editText = addEditText("输入内容");
        editText.setHint("输入内容");
        editText.setLines(5);
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setTextCursorDrawable(R.drawable.edit_text_cursor);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        final String noteContent = SPUtils.getInstance().getString(KEY_NOTE_CONTENT);
        if (!TextUtils.isEmpty(noteContent)) {
            editText.setText(noteContent);
        }
        editText.addTextChangedListener(new TextWatcher() {
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
        editText.setOnEditorActionListener((v, actionId, event) -> {
            LogControl.d("actionId:" + actionId);
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == 0) {

                final String string = editText.getText().toString();
                LogControl.d("string:" + string);
                SPUtils.getInstance().put(KEY_NOTE_CONTENT, string.trim());
                hideInputMethod(editText);
                NotificationHelper.getInstance().show();
                return false;
            }
            return false;
        });

    }

    /**
     * 其他内容
     */
    private void initNoteET() {
        EditText editText = addEditText("输入内容");
        editText.setHint("输入内容");
        editText.setLines(5);
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setTextCursorDrawable(R.drawable.edit_text_cursor);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        final String noteContent = SPUtils.getInstance().getString(KEY_NOTE_CONTENT_2);
        if (!TextUtils.isEmpty(noteContent)) {
            editText.setText(noteContent);
        }
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                SPUtils.getInstance().put(KEY_NOTE_CONTENT_2, s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // 监听键盘的完成事件
        editText.setOnEditorActionListener((v, actionId, event) -> {
            LogControl.d("actionId:" + actionId);
            if (actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == 0) {

                final String string = editText.getText().toString();
                LogControl.d("string:" + string);
                SPUtils.getInstance().put(KEY_NOTE_CONTENT_2, string.trim());
                hideInputMethod(editText);
                return false;
            }
            return false;
        });
    }

    private void hideInputMethod(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getFragmentManager().findFragmentByTag(WebITHomeFG.class.getSimpleName());
        LogControl.d("currentFragment:" + currentFragment);
        if (currentFragment instanceof WebITHomeFG) {
            WebView webView = ((WebITHomeFG) currentFragment).getWebView();
            if (webView != null && webView.canGoBack()) {
                webView.goBack(); // 返回 WebView 的上一页
            } else {
                super.onBackPressed(); // 如果无法返回上一页，则关闭当前 Activity
            }
        } else {
            super.onBackPressed(); // 如果不是 WebITHomeFG，则默认处理返回键
        }
    }

}
