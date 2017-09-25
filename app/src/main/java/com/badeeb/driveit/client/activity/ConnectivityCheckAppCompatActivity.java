package com.badeeb.driveit.client.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.badeeb.driveit.client.R;
import com.badeeb.driveit.client.controllers.DriveItApplication;

/**
 * Created by meldeeb on 9/25/17.
 */

public abstract class ConnectivityCheckAppCompatActivity extends AppCompatActivity {

    private NetworkChangeReceiver networkChangeReceiver;
    private LocationChangeReceiver locationChangeReceiver;

    private AlertDialog connectionDisabledWarningDialog;
    private AlertDialog locationDisabledWarningDialog;
    public static final String ACTION_LOG_OUT = "com.blink22.carpool.activities.ConnectivityCheckAppCompatActivity.BROADCAST.LOGOUT";
    private boolean isConnected;


    @Override
    protected void onResume() {
        super.onResume();
        networkChangeReceiver = new NetworkChangeReceiver();
        locationChangeReceiver = new LocationChangeReceiver();
        isConnected = isConnected();

        registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(locationChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        checkLocationService();
        checkInternetConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
        unregisterReceiver(locationChangeReceiver);
    }

    private void checkLocationService() {
        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            if (!gpsEnabled) {
                if (locationDisabledWarningDialog == null || !locationDisabledWarningDialog.isShowing()) {
                    showGPSDisabledWarningDialog();
                }
            } else if (locationDisabledWarningDialog != null && locationDisabledWarningDialog.isShowing()) {
                locationDisabledWarningDialog.dismiss();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkInternetConnection() {
        boolean isConnected = isConnected();

//        if (!isConnected) {
//            if (getSupportActionBar() != null) {
//                changeStatusBarColor(R.color.red);
//                getSupportActionBar().show();
//            }
//            this.isConnected = false;
//        } else {
//            // It is connected
//            if (getSupportActionBar() != null) {
//                changeStatusBarColor(R.color.colorAccent);
//                getSupportActionBar().hide();
//            }
//            // It is now connected but it wasn't connected
//            if (!this.isConnected)
//                onConnectedToInternet();
//            this.isConnected = true;
//        }
    }

    private boolean isConnected() {
        ConnectivityManager
                cm = (ConnectivityManager) DriveItApplication.getInstance().getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void showGPSDisabledWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.GPS_disabled_warning_title);
        builder.setMessage(R.string.GPS_disabled_warning_msg);
        builder.setPositiveButton(R.string.ok_btn_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        });
        builder.setCancelable(false);
        locationDisabledWarningDialog = builder.create();
        locationDisabledWarningDialog.show();
    }

    private void showConnectionDisabledWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.internet_disabled_warning_title);
        builder.setMessage(R.string.internet_disabled_warning_msg);
        builder.setPositiveButton(R.string.ok_btn_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent i = new Intent(Settings.ACTION_SETTINGS);
                startActivity(i);
            }
        });
        builder.setCancelable(false);
        connectionDisabledWarningDialog = builder.create();
        connectionDisabledWarningDialog.show();
    }

    private final class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION))
                checkLocationService();
        }
    }

    private final class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION))
                checkInternetConnection();
        }
    }

    private void setActionbarFeatures(View customView, ActionBar actionBar) {
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        Toolbar parent = (Toolbar) customView.getParent();
        parent.setContentInsetsAbsolute(0, 0);
    }

    private void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(color));
        }
    }

    public abstract void onConnectedToInternet();

}
