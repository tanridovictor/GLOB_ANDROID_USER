package com.enseval.gcmuser.Activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.enseval.gcmuser.Fragment.HomeFragment;
import com.enseval.gcmuser.R;

public class SplashScreenActivity extends AppCompatActivity {
    private int waktu_loading=2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
                finish();
            }
        },waktu_loading);
    }
}
