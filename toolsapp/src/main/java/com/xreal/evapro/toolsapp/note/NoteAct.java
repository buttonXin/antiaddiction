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

import java.util.List;

public class NoteAct extends BaseOLActivity {

    public static final String KEY_NOTE_CONTENT = "KEY_NOTE_CONTENT";
    public static final String KEY_NOTE_CONTENT_2 = "KEY_NOTE_CONTENT_2";
    public static final String KEY_NOTE_SHOW_IT = "KEY_NOTE_SHOW_IT";


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

        if (SPUtils.getInstance().getBoolean("KEY_NOTE_SHOW_IT", false)) {
            addButton("IT之家", 1, v -> new WebITHomeFG().setBaseParams("0").openFragment(getFragmentManager()));
        }


        initNotificationET();
        addLine();
        initNoteET();
        addLine();

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

                if (!TextUtils.isEmpty(string) && string.contains("IT之家")) {
                    SPUtils.getInstance().put("KEY_NOTE_SHOW_IT", true);
                    toast("退出后显示 IT之家");
                }
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

        final List<Fragment> fragments = getFragmentManager().getFragments();
        final int size = fragments.size();
        if (size > 0) {
            final Fragment fragment = fragments.get(size - 1);
            LogControl.d("current fragment " + fragment.getClass().getSimpleName());
            if (fragment instanceof WebITHomeFG) {
                WebView webView = ((WebITHomeFG) fragment).getWebView();
                if (webView != null && webView.canGoBack()) {
                    LogControl.d("goBack");
                    webView.goBack(); // 返回 WebView 的上一页
                    return;
                }
            }
            LogControl.d("remove fragment " + fragment.getClass().getSimpleName());
            getFragmentManager().beginTransaction().remove(fragment).commit();
            return;
        }

        if (hasPrevActivity()) {
            finish();
        } else {
            moveTaskToBack(true);
            finish();
        }

    }

}
