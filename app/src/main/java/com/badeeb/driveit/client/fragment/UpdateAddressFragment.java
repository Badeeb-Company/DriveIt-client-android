package com.badeeb.driveit.client.fragment;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.badeeb.driveit.client.R;
import com.badeeb.driveit.client.activity.MainActivity;
import com.badeeb.driveit.client.model.JsonUpdateAddress;
import com.badeeb.driveit.client.network.MyVolley;
import com.badeeb.driveit.client.shared.AppPreferences;
import com.badeeb.driveit.client.shared.AppSettings;
import com.badeeb.driveit.client.shared.OnPermissionsGrantedHandler;
import com.badeeb.driveit.client.shared.PermissionsChecker;
import com.badeeb.driveit.client.shared.UiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateAddressFragment extends Fragment {

    public static final String TAG = UpdateAddressFragment.class.getSimpleName();
    private static final int PERM_LOCATION_RQST_CODE = 100;
    private static final long FETCH_LOCATION_TIMEOUT = 10 * 1000; // last known location = null

    private Location mCurrentLocation;
    private String mCurrentAddress;

    private MainActivity mActivity;
    private Context mContext;
    AppSettings mAppSettings;
    private Button bUpdateAddress;
    private OnPermissionsGrantedHandler onLocationPermissionGrantedHandler;
    private LocationManager locationManager;
    private AlertDialog locationDisabledWarningDialog;
    private UpdateAddressFragment.LocationChangeReceiver locationChangeReceiver;
    private GoogleApiClient mGoogleApiClient;
    private TimerTask cancelFetchLocationTask;
    private LocationListener locationListener;
    private ProgressDialog mProgressDialog;

    public UpdateAddressFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update_address, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        mActivity = (MainActivity) getActivity();
        mContext = getContext();
        mAppSettings = AppSettings.getInstance();
        bUpdateAddress = view.findViewById(R.id.update_address);
        onLocationPermissionGrantedHandler = createOnLocationPermissionGrantedHandler();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationChangeReceiver = new UpdateAddressFragment.LocationChangeReceiver();
        locationListener = createLocationListener();
        mProgressDialog = UiUtils.createProgressDialog(mActivity);

        setupListener();

        Log.d(TAG, "init - End");
    }

    private void setupListener() {
        Log.d(TAG, "setupListener - Start");

        bUpdateAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListener - bUpdateAddress_onclick - Start");
                PermissionsChecker.checkPermissions(UpdateAddressFragment.this, onLocationPermissionGrantedHandler,
                        PERM_LOCATION_RQST_CODE, Manifest.permission.ACCESS_FINE_LOCATION);
                Log.d(TAG, "setupListener - bUpdateAddress_onclick - End");
            }
        });

        Log.d(TAG, "setupListener - End");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult - Start");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERM_LOCATION_RQST_CODE:
                if(PermissionsChecker.permissionsGranted(grantResults)){
                    onLocationPermissionGrantedHandler.onPermissionsGranted();
                }
                break;
        }

        Log.d(TAG, "onRequestPermissionsResult - End");
    }

    private OnPermissionsGrantedHandler createOnLocationPermissionGrantedHandler() {

        return new OnPermissionsGrantedHandler() {
            @Override
            public void onPermissionsGranted() {
                checkLocationService();
            }
        };

    }

    private boolean checkLocationService() {
        Log.d(TAG, "checkLocationService - Start");

        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            if (locationDisabledWarningDialog == null || !locationDisabledWarningDialog.isShowing()) {
                showGPSDisabledWarningDialog();
                getActivity().registerReceiver(locationChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
            }
        } else {
            if (locationDisabledWarningDialog != null && locationDisabledWarningDialog.isShowing()) {
                locationDisabledWarningDialog.dismiss();
            }
            else {
                // Get current location
                Log.d(TAG, "checkLocationService - Get current location");
                initGoogleApiClient();
                onFindingLocation();
            }
        }

        Log.d(TAG, "checkLocationService - End");

        return gpsEnabled;
    }

    private void showGPSDisabledWarningDialog() {

        Log.d(TAG, "showGPSDisabledWarningDialog - Start");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
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

        Log.d(TAG, "showGPSDisabledWarningDialog - End");
    }

    private final class LocationChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(LocationManager.PROVIDERS_CHANGED_ACTION)){
                Log.d(TAG, "LocationChangeReceiver - onReceive - Start");

                checkLocationService();
                getActivity().unregisterReceiver(this);

                Log.d(TAG, "LocationChangeReceiver - onReceive - End");
            }
        }
    }

    private void initGoogleApiClient() {

        Log.d(TAG, "initGoogleApiClient - Start");

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        fetchUserCurrentLocation();
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                        Toast.makeText(getActivity(), "API client connection suspended", Toast.LENGTH_LONG).show();
                    }

                }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(getActivity(), "API client connection failed", Toast.LENGTH_LONG).show();
                    }
                })
                .build();

        Log.d(TAG, "initGoogleApiClient - End");
    }


    /*
    We try to get the last known location if found then no problem, if not found then we
    will set current location to default location(Australia) and register location update
    to get the current location whenever received.
 */
    @SuppressWarnings({"MissingPermission"})
    private void fetchUserCurrentLocation() {

        Log.d(TAG, "fetchUserCurrentLocation - Start");

        cancelFetchLocationTask = new TimerTask() {
            @Override
            public void run() {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mCurrentLocation == null) {
                            onLocationNotFound();
                        } else {
                            onLocationFound();
                        }
                    }
                });
            }
        };
        registerLocationUpdate();
        Timer timer = new Timer();
        timer.schedule(cancelFetchLocationTask, FETCH_LOCATION_TIMEOUT);

        Log.d(TAG, "fetchUserCurrentLocation - End");
    }

    @SuppressWarnings({"MissingPermission"})
    protected void registerLocationUpdate() {
        Log.d(TAG, "registerLocationUpdate - Start");

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(0);
        request.setInterval(0);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, locationListener);

        Log.d(TAG, "registerLocationUpdate - End");
    }

    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "onLocationChanged - Start");

                if(cancelFetchLocationTask != null){
                    cancelFetchLocationTask.cancel();
                }
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mCurrentLocation = location;
                onLocationFound();

                Log.d(TAG, "onLocationChanged - End");
            }
        };
    }

    @SuppressWarnings({"MissingPermission"})
    private void onFindingLocation() {
        Log.d(TAG, "onFindingLocation - Start");
        mProgressDialog.show();
        disconnectGoogleApiClient();
        mGoogleApiClient.connect();
        Log.d(TAG, "onFindingLocation - End");
    }

    private void onLocationFound() {
        Log.d(TAG, "onLocationFound - Start");

        mCurrentAddress = getLocationAddress();

        DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mActivity.getClient().setLocationAddr(mCurrentAddress);
                mActivity.getClient().setLocationLat(mCurrentLocation.getLatitude());
                mActivity.getClient().setLocationLng(mCurrentLocation.getLongitude());

                updateUserLocationApi();
            }
        };

        DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        };

        locationDisabledWarningDialog = UiUtils.showDialog(getContext(), R.style.DialogTheme,
                R.string.pickup_location, mCurrentAddress,
                R.string.confirm_btn_dialog, positiveListener, R.string.no_msg, negativeListener);

        mProgressDialog.dismiss();

        Log.d(TAG, "onLocationFound - End");
    }


    private void onLocationNotFound() {
        Log.d(TAG, "onLocationNotFound - Start");
        mProgressDialog.dismiss();
        disconnectGoogleApiClient();

        UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.location_not_found, R.string.ok_btn_dialog, null);
        Log.d(TAG, "onLocationNotFound - End");
    }

    private void disconnectGoogleApiClient(){
        Log.d(TAG, "disconnectGoogleApiClient - Start");

        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }

        Log.d(TAG, "disconnectGoogleApiClient - End");
    }

    private String getLocationAddress() {
        Log.d(TAG, "getLocationAddress - Start");
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        String address = "";
        try {
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            addresses = geocoder.getFromLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                Address mainAddress = addresses.get(0);
                for (int i = 0; i < mainAddress.getMaxAddressLineIndex(); i++) {
                    address += mainAddress.getAddressLine(i) + ", ";
                }
                address += mainAddress.getAddressLine(mainAddress.getMaxAddressLineIndex());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "getLocationAddress - End");

        return address;
    }

    private void updateUserLocationApi() {
        Log.d(TAG, "updateUserLocationApi - Start");
        try {
            String url = AppPreferences.BASE_URL + "/client";

            JsonUpdateAddress request = new JsonUpdateAddress();
            request.setLat(mCurrentLocation.getLatitude()+"");
            request.setLng(mCurrentLocation.getLongitude()+"");
            request.setAddress(mCurrentAddress);

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "updateUserLocationApi - Json Request" + gson.toJson(request));

            // Call request Truck service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, jsonObject,

                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "updateUserLocationApi - onResponse - Start");
                            Log.d(TAG, "updateUserLocationApi - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonUpdateAddress jsonResponse = gson.fromJson(responseData, JsonUpdateAddress.class);

                            Log.d(TAG, "updateUserLocationApi - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "updateUserLocationApi - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Save user to shared preferences
                                mAppSettings.saveUser(mActivity.getClient());

                                UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.address_update_dialog_msg, R.string.ok_btn_dialog, null);

                                // Return to home screen
                                getFragmentManager().popBackStack();

                            } else {
                                // Invalid login
                                UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.address_update_error_dialog_msg, R.string.ok_btn_dialog, null);
                            }

                            Log.d(TAG, "updateUserLocationApi - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "updateUserLocationApi - onErrorResponse: " + error.toString());

                            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                                // Authorization issue
                                UiUtils.showDialog(mContext, R.style.DialogTheme, R.string.account_not_active, R.string.ok_btn_dialog, null);
                                mAppSettings.clearUserInfo();
                                mAppSettings.clearTripInfo();
                                goToLogin();

                            } else if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonUpdateAddress jsonResponse = gson.fromJson(responseData, JsonUpdateAddress.class);

                                Log.d(TAG, "updateUserLocationApi - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "updateUserLocationApi - Error Message: " + jsonResponse.getJsonMeta().getMessage());

                                Toast.makeText(getContext(), jsonResponse.getJsonMeta().getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
            ) {

                /**
                 * Passing some request headers
                 */
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    headers.put("Accept", "*");
                    headers.put("Authorization", "Token token=" + mActivity.getClient().getToken());

                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "updateUserLocationApi - End");
    }

    private void goToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.commit();
    }
}
