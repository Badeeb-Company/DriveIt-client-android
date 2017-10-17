package com.badeeb.driveit.client.fragment;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.badeeb.driveit.client.R;
import com.badeeb.driveit.client.activity.MainActivity;
import com.badeeb.driveit.client.shared.AppPreferences;
import com.badeeb.driveit.client.shared.OnPermissionsGrantedHandler;
import com.badeeb.driveit.client.shared.PermissionsChecker;
import com.badeeb.driveit.client.shared.UiUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripRequestFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = TripRequestFragment.class.getSimpleName();

    // Class Attributes
    private MainActivity mActivity;

    public TripRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_request, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu - Start");

        super.onPrepareOptionsMenu(menu);

        MenuItem logout = menu.findItem(R.id.nav_logout);
        logout.setVisible(true);

        Log.d(TAG, "onPrepareOptionsMenu - End");
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        mActivity = (MainActivity) getActivity();

//        ((MainActivity) getActivity()).getClient().getId();

        // Setup Listeners
        setupListeners(view);

        // Refresh menu toolbar
        mActivity.enbleNavigationView();

        Log.d(TAG, "init - End");
    }

    public void goToRequestDialog() {
        RequestDialogFragment requestDialogFragment = new RequestDialogFragment();
        requestDialogFragment.setCancelable(false);
        FragmentManager fragmentManager = getFragmentManager();
        requestDialogFragment.show(fragmentManager, requestDialogFragment.TAG);
    }

    public void setupListeners(View view) {
        // Request Truck button
        final Button requestTruck = (Button) view.findViewById(R.id.request_truck);
        requestTruck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((mActivity.getClient().getLocationAddr() == null || mActivity.getClient().getLocationAddr().isEmpty())
                        && mActivity.getClient().getLocationLat() == 0.0
                        && mActivity.getClient().getLocationLng() == 0.0) {
                    // Address need to be updated
                    requestAddressUpdate();
                }
                else {
                    goToRequestDialog();
                }
            }
        });
    }

    private void requestAddressUpdate() {
        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                goToUpdateAddress();
            }
        };

        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        };

        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.update_address_msg, R.string.update_address_des_msg,
                R.string.yes_msg, positiveListener, R.string.no_msg, negativeListener);
    }

    private void goToUpdateAddress() {
        UpdateAddressFragment updateAddressFragment = new UpdateAddressFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.main_frame, updateAddressFragment, updateAddressFragment.TAG);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();
    }

}
