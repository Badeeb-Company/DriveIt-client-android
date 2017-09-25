package com.badeeb.driveit.client.controllers;

import android.app.Application;

import com.badeeb.driveit.client.shared.Settings;

/**
 * Created by meldeeb on 9/25/17.
 */

public class DriveItApplication extends Application {
    private static DriveItApplication sDriverItApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sDriverItApplication = this;
    }

    public static DriveItApplication getInstance() {
        if (sDriverItApplication == null) {
            sDriverItApplication = new DriveItApplication();
        }
        return sDriverItApplication;
    }

    public static boolean isLoggedIn() {
        return Settings.getInstance().isLoggedIn();
    }

}
