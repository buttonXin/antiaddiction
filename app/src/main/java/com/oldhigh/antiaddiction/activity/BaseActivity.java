package com.oldhigh.antiaddiction.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity  extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layoutId());

        initView();

        initData();

    }

    protected abstract int layoutId();

    protected abstract void initView();

    protected abstract void initData();




}
