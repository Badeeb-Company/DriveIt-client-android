package com.badeeb.driveit.client.shared;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meldeeb on 9/25/17.
 */

public class PermissionsChecker {

    public static void checkPermissions(Fragment fragment, OnPermissionsGrantedHandler handler, int requestCode,
                                        String... permissions) {
        boolean granted = true;
        List<String> toBeGranted = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(fragment.getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                toBeGranted.add(permission);
                granted = false;
            }
        }
        if (!granted) {
            fragment.requestPermissions(toBeGranted.toArray(new String[toBeGranted.size()]), requestCode);
        } else {
            handler.onPermissionsGranted();
        }
    }

    public static boolean permissionsGranted(int[] grantResults){
        boolean granted = true;
        for (int result: grantResults) {
            if(result != PackageManager.PERMISSION_GRANTED){
                granted = false;
                break;
            }
        }
        return  granted;
    }

}