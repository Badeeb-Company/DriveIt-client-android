package com.badeeb.driveit.client.activity;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.badeeb.driveit.client.R;
import com.badeeb.driveit.client.shared.AppPreferences;

public class SplashActivity extends AppCompatActivity {

    // Logging Purpose
    private final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate - Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int splashTimeOut = AppPreferences.SPLASH_TIME_OUT;

        new Handler().postDelayed(new Runnable() {

            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

            @Override
            public void run() {

                Log.d(TAG, "run - Start");

                // This method will be executed once the timer is over
                // Start your app main activity
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);

                // close this activity
                finish();

                Log.d(TAG, "run - End");
            }
        }, splashTimeOut);

        Log.d(TAG, "onCreate - End");
    }
}
