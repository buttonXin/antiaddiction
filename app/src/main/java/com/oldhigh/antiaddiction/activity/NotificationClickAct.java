package com.oldhigh.antiaddiction.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.oldhigh.antiaddiction.util.AudioPlayer;

public class NotificationClickAct extends AppCompatActivity {

    private static final String TAG = NotificationClickAct.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(TAG, "onCreate: " );

        AudioPlayer.getInstance().stop();

        //  跳转到系统桌面
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);

        finish();

    }
}
