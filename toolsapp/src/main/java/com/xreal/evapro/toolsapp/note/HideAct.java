package com.xreal.evapro.toolsapp.note;

import static com.xreal.evapro.toolsapp.note.NoteAct.KEY_NOTE_CONTENT;

import android.app.Activity;
import android.os.Bundle;

import com.xreal.evapro.toolsapp.util.NotificationHelper;
import com.xreal.evapro.toolsapp.util.SPUtils;

/**
 * 隐藏的activity
 */
public class HideAct extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.getInstance().changeContent(SPUtils.getInstance().getString(KEY_NOTE_CONTENT));

        finish();
    }
}
