package com.oldhigh.antiaddiction.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.oldhigh.antiaddiction.R;

public class MainActivity extends AppCompatActivity {

    private LinearLayout llContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llContent = findViewById(R.id.ll_content);


        initData();
    }

    private void initData() {
        addButton("选择应用", view -> startActivity(new Intent(getApplicationContext(),ChooseActivity.class)));

        addButton("查看应用", view -> startActivity(new Intent(getApplicationContext(),SelectedActivity.class)));
    }


    private void addButton(String text, View.OnClickListener listener){
        Button button = new Button(this);
        button.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        button.setOnClickListener(listener);
        llContent.addView(button,params);
    }
}