package com.badeeb.driveit.client.fragment;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.badeeb.driveit.client.model.JsonCancelTrip;
import com.badeeb.driveit.client.model.JsonLogin;
import com.badeeb.driveit.client.model.JsonRequestTrip;
import com.badeeb.driveit.client.model.Trip;
import com.badeeb.driveit.client.network.MyVolley;
import com.badeeb.driveit.client.shared.AppPreferences;
import com.badeeb.driveit.client.shared.FirebaseManager;
import com.badeeb.driveit.client.shared.Settings;
import com.badeeb.driveit.client.shared.UiUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link DialogFragment} subclass.
 */
public class RequestDialogFragment extends DialogFragment {


    private static final long FETCH_LOCATION_TIMEOUT = 10 * 1000; // last known location = null

    private Location mCurrentLocation;
    private String mCurrentAddress;

    // Logging Purpose
    public static final String TAG = RequestDialogFragment.class.getSimpleName();

    // attributes that will be used for JSON calls
    private String url = AppPreferences.BASE_URL + "/trip";

    // Class Attributes
//    private User client;
    private Trip mtrip;
    private boolean paused;
    private RequestStatus requestStatus;
    private ProgressBar mProgressBar;
    private TextView tvLoadingMessage;
    private TextView tvAddress;
    private TextView tvYourLocation;
    private LinearLayout llLoading;
    private LinearLayout llConfirmRide;
    private Button bConfirmRide;
    private Button bCancelLoading;
    private Button bCancelDialog;

    // Firebase database reference
    private FirebaseManager firebaseManager;
    private DatabaseReference tripReference;
    private ValueEventListener tripEventListener;
    private TimerTask cancelFetchLocationTask;

    private GoogleApiClient mGoogleApiClient;
    private LocationListener locationListener;
    private Settings settings;

    public RequestDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_request_dialog, container, false);
        init(view);

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        tvLoadingMessage = (TextView) view.findViewById(R.id.tvLoadingMessage);
        llLoading = (LinearLayout) view.findViewById(R.id.llLoading);
        llConfirmRide = (LinearLayout) view.findViewById(R.id.llConfirmRide);
        tvAddress = (TextView) view.findViewById(R.id.tvAddress);
        tvYourLocation = (TextView) view.findViewById(R.id.tvYourLocation);
        bConfirmRide = (Button) view.findViewById(R.id.bConfirmRide);
        mProgressBar = view.findViewById(R.id.progressBar);
        bCancelLoading = (Button) view.findViewById(R.id.bCancelLoading);
        bCancelDialog = (Button) view.findViewById(R.id.bCancelDialog);

        paused = false;

        bCancelLoading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (requestStatus) {
                    case FINDING_LOCATION:
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, locationListener);
                        disconnectGoogleApiClient();
                        dismiss();
                        break;
                    case FINDING_DRIVERS:
                        removeTripListener();
                        disconnectGoogleApiClient();
                        cancelRide();
                        break;
                }
            }
        });

        bConfirmRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestStatus == RequestStatus.LOCATION_FOUND) {
                    onFindingDrivers();
                } else if (requestStatus == RequestStatus.LOCATION_NOT_FOUND) {
                    onFindingLocation();
                } else if (requestStatus == RequestStatus.NOT_SERVED) {
                    onFindingLocation();
                }
            }
        });

        bCancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                disconnectGoogleApiClient();
                dismiss();
            }
        });

        firebaseManager = new FirebaseManager();
        locationListener = createLocationListener();
        settings = Settings.getInstance();
        initGoogleApiClient();

        onFindingLocation();
    }

    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(cancelFetchLocationTask != null){
                    cancelFetchLocationTask.cancel();
                }
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                mCurrentLocation = location;
                onLocationFound();
            }
        };
    }

    private void initGoogleApiClient() {
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
    }

    /*
        We try to get the last known location if found then no problem, if not found then we
        will set current location to default location(Australia) and register location update
        to get the current location whenever received.
     */
    @SuppressWarnings({"MissingPermission"})
    private void fetchUserCurrentLocation() {
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
    }

    @SuppressWarnings({"MissingPermission"})
    protected void registerLocationUpdate() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setSmallestDisplacement(0);
        request.setInterval(0);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, locationListener);
    }

    private void onFindingDrivers() {
        requestStatus = RequestStatus.FINDING_DRIVERS;
        UiUtils.show(llLoading);
        UiUtils.hide(llConfirmRide);
        UiUtils.hide(bCancelLoading);
        tvLoadingMessage.setText(getActivity().getString(R.string.searching_drivers));
        requestTruck();
    }

    @SuppressWarnings({"MissingPermission"})
    private void onFindingLocation() {
        requestStatus = RequestStatus.FINDING_LOCATION;
        UiUtils.show(llLoading);
        UiUtils.hide(llConfirmRide);
        disconnectGoogleApiClient();
        mGoogleApiClient.connect();
        tvLoadingMessage.setText(getActivity().getString(R.string.fetch_location));
    }

    private void onLocationFound() {
        requestStatus = RequestStatus.LOCATION_FOUND;
        mCurrentAddress = getLocationAddress();
        UiUtils.hide(llLoading);
        UiUtils.show(llConfirmRide);
        UiUtils.show(tvAddress);
        tvYourLocation.setText("Your pickup location is");
        bConfirmRide.setText("Confirm");
        tvAddress.setText(mCurrentAddress);
    }

    private void onLocationNotFound() {
        requestStatus = RequestStatus.LOCATION_NOT_FOUND;
        disconnectGoogleApiClient();
        UiUtils.hide(llLoading);
        UiUtils.show(llConfirmRide);
        UiUtils.hide(tvAddress);
        tvYourLocation.setText("Cannot find your current location");
        bConfirmRide.setText("Try again");
    }

    private void disconnectGoogleApiClient(){
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    private void requestTruck() {
        Log.d(TAG, "requestTruck - Start");
        try {
            JsonRequestTrip request = new JsonRequestTrip();
            request.setLat(mCurrentLocation.getLatitude() + "");
            request.setLng(mCurrentLocation.getLongitude() + "");
            request.setDestination(mCurrentAddress);

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "requestTruck - Json Request" + gson.toJson(request));

            // Call request Truck service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,

                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "requestTruck - onResponse - Start");
                            Log.d(TAG, "requestTruck - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                            Log.d(TAG, "requestTruck - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "requestTruck - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Move to next screen
                                mtrip = new Trip();
                                mtrip.setId(jsonResponse.getTripId());
                                mtrip.setDestination(mCurrentAddress);
                                mtrip.setLng(mCurrentLocation.getLongitude());
                                mtrip.setLat(mCurrentLocation.getLatitude());
                                mtrip.setClientId(((MainActivity) getActivity()).getClient().getId());
                                UiUtils.show(bCancelLoading);
                                setupListeners();
                            } else {
                                // Invalid login
                                Toast.makeText(getContext(), getString(R.string.try_error), Toast.LENGTH_LONG).show();
                            }

                            Log.d(TAG, "requestTruck - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "requestTruck - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonRequestTrip jsonResponse = gson.fromJson(responseData, JsonRequestTrip.class);

                                Log.d(TAG, "requestTruck - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "requestTruck - Error Message: " + jsonResponse.getJsonMeta().getMessage());

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
                    headers.put("Authorization", "Token token=" + ((MainActivity) getActivity()).getClient().getToken());

                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "requestTruck - End");
    }

    private String getLocationAddress() {
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
        return address;
    }

    public void setupListeners() {
        tripEventListener = createValueEventListener();
        tripReference = firebaseManager.createChildReference(FirebaseManager.CLIENTS_KEY,
                String.valueOf(((MainActivity) getActivity()).getClient().getId()), FirebaseManager.TRIP_KEY);
        tripReference.addValueEventListener(tripEventListener);
    }

    private ValueEventListener createValueEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "setupListeners - mdatabase_onDataChange - Start");
                if (dataSnapshot.getValue() != null) {
                    Trip fdbTrip = dataSnapshot.getValue(Trip.class);
                    if (fdbTrip.getState().equals(AppPreferences.TRIP_ACCEPTED)) {
                        fdbTrip.setLng(mtrip.getLng());
                        fdbTrip.setLat(mtrip.getLat());
                        fdbTrip.setDestination(mtrip.getDestination());
                        fdbTrip.setClientId(mtrip.getClientId());
                        mtrip = fdbTrip;
                        onTripAccepted();
                    } else if (fdbTrip.getState().equals(AppPreferences.TRIP_NOT_SERVED)) {
                        onTripNotServed();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        };
    }

    private void onTripAccepted() {
        requestStatus = RequestStatus.ACCEPTED;
        removeTripListener();
        disconnectGoogleApiClient();
        if (!paused) {
            TripDetailsFragment tripDetailsFragment = new TripDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable("trip", Parcels.wrap(mtrip));
            tripDetailsFragment.setArguments(bundle);

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.main_frame, tripDetailsFragment, tripDetailsFragment.TAG);
            fragmentTransaction.commit();

            dismiss();
        }
    }

    private void onTripNotServed() {
        requestStatus = RequestStatus.NOT_SERVED;
        removeTripListener();
        disconnectGoogleApiClient();
        UiUtils.hide(llLoading);
        UiUtils.show(llConfirmRide);
        UiUtils.hide(tvAddress);
        tvYourLocation.setText("Cannot find nearby drivers");
        bConfirmRide.setText("Try again");
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        if (requestStatus == RequestStatus.ACCEPTED) {
            onTripAccepted();
        } else {
            getDialog().getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.dialog_rounded_corner));
        }
    }

    private void cancelRide() {
        Log.d(TAG, "cancelRide - Start");

        try {
            url += "/" + mtrip.getId() + "/cancel";

            JsonCancelTrip request = new JsonCancelTrip();
            request.setTripId(mtrip.getId());

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "cancelRide - Json Request" + gson.toJson(request));

            // Call request Truck service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "cancelRide - onResponse - Start");

                            Log.d(TAG, "cancelRide - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonCancelTrip jsonResponse = gson.fromJson(responseData, JsonCancelTrip.class);

                            Log.d(TAG, "cancelRide - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "cancelRide - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Move to next screen

                            } else {
                                // Invalid login
                                Toast.makeText(getContext(), getString(R.string.try_error), Toast.LENGTH_SHORT).show();
                            }

                            Log.d(TAG, "cancelRide - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "cancelRide - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonLogin jsonResponse = gson.fromJson(responseData, JsonLogin.class);

                                Log.d(TAG, "cancelRide - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "cancelRide - Error Message: " + jsonResponse.getJsonMeta().getMessage());

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
                    headers.put("Authorization", "Token token=" + ((MainActivity) getActivity()).getClient().getToken());
                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestDialogFragment.this.dismiss();

        Log.d(TAG, "cancelRide - End");
    }

    private void removeTripListener() {
        tripReference.removeEventListener(tripEventListener);
    }

    private enum RequestStatus {FINDING_LOCATION, LOCATION_FOUND, LOCATION_NOT_FOUND, FINDING_DRIVERS, ACCEPTED, NOT_SERVED}

}
