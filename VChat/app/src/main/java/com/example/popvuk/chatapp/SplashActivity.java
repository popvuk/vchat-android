package com.example.popvuk.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.popvuk.vchat.R;

/**
 * Created by MiljanaMilena on 2/5/2018.
 */

public class SplashActivity  extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent main = new Intent("android.intent.action.HOME");
        startActivity(main);
        finish();
    }
}
