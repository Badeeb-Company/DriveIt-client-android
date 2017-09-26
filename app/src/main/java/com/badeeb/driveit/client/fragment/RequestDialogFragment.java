package com.badeeb.driveit.client.fragment;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
import com.badeeb.driveit.client.shared.UiUtils;
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


    private static final long FETCH_LOCATION_TIMEOUT_1 = 10 * 1000; // last known location = null
    private static final long FETCH_LOCATION_TIMEOUT_2 = 5 * 1000;  // last known location != null

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
    private LocationManager locationManager;
    private LocationListener locationListener;
    private TimerTask fetchLocationTask;

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
                        locationManager.removeUpdates(locationListener);
                        dismiss();
                        break;
                    case FINDING_DRIVERS:
                        removeTripListener();
                        cancelRide();
                        break;
                }
            }
        });

        bConfirmRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (requestStatus == RequestStatus.LOCATION_FOUND){
                    tvLoadingMessage.setText(getActivity().getString(R.string.searching_drivers));
                    UiUtils.show(llLoading);
                    UiUtils.hide(llConfirmRide);
                    UiUtils.hide(bCancelLoading);
                    requestStatus = RequestStatus.FINDING_DRIVERS;
                    requestTruck();
                } else if(requestStatus == RequestStatus.LOCATION_NOT_FOUND){
                    requestStatus = RequestStatus.FINDING_LOCATION;
                    onFindingLocation();
                } else if(requestStatus == RequestStatus.NOT_SERVED){

                }
            }
        });

        bCancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = createLocationListener();
        firebaseManager = new FirebaseManager();

        onFindingLocation();
    }

    @SuppressWarnings({"MissingPermission"})
    private void onFindingLocation() {
        requestStatus = RequestStatus.FINDING_LOCATION;
        UiUtils.show(llLoading);
        UiUtils.hide(llConfirmRide);
        tvLoadingMessage.setText(getActivity().getString(R.string.fetch_location));
        mCurrentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        fetchLocationTask = new TimerTask() {
            @Override
            public void run() {
                locationManager.removeUpdates(locationListener);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(mCurrentLocation == null){
                            onLocationNotFound();
                        } else {
                            onLocationFound();
                        }
                    }
                });
            }
        };
        Timer timer = new Timer();
        // timeout of fetching location is bigger when there was no last known location found
        long taskTimeout = mCurrentLocation == null ? FETCH_LOCATION_TIMEOUT_1 : FETCH_LOCATION_TIMEOUT_2;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        timer.schedule(fetchLocationTask, taskTimeout);
    }

    private void onLocationFound() {
        requestStatus = RequestStatus.LOCATION_FOUND;
        mCurrentAddress = getLocationAddress();
        UiUtils.hide(llLoading);
        UiUtils.show(llConfirmRide);
        UiUtils.show(tvAddress);
        tvYourLocation.setText("Your pickup location is");
        tvAddress.setText(mCurrentAddress);
    }

    private void onLocationNotFound() {
        requestStatus = RequestStatus.LOCATION_NOT_FOUND;
        UiUtils.hide(llLoading);
        UiUtils.show(llConfirmRide);
        UiUtils.hide(tvAddress);
        tvYourLocation.setText("Cannot find your current location");
        bConfirmRide.setText("Try again");
    }

    private LocationListener createLocationListener() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (fetchLocationTask != null) {
                    fetchLocationTask.cancel();
                }
                locationManager.removeUpdates(this);
                mCurrentLocation = location;
                onLocationFound();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
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
                    // Check if trip is accepted or rejected
                    if (fdbTrip.getState().equals(AppPreferences.TRIP_ACCEPTED)) {
                        // Trip accepted
                        Toast.makeText(getContext(), R.string.driver_found, Toast.LENGTH_SHORT).show();

                        // Move to fragment that will display driver details
                        fdbTrip.setLng(mtrip.getLng());
                        fdbTrip.setLat(mtrip.getLat());
                        fdbTrip.setDestination(mtrip.getDestination());
                        fdbTrip.setClientId(mtrip.getClientId());

                        mtrip = fdbTrip;
                        requestStatus = RequestStatus.ACCEPTED;
                        if (!paused) {
                            onTripAccepted();
                        }
                    } else if (fdbTrip.getState().equals(AppPreferences.TRIP_NOT_SERVED)) {
                        // Trip rejected
                        Toast.makeText(getContext(), R.string.request_again, Toast.LENGTH_LONG).show();
                        requestStatus = RequestStatus.NOT_SERVED;
                        if (!paused) {
                            onTripNotServed();
                        }
                    }
                    // Remove firebase database listener
                }

                Log.d(TAG, "setupListeners - mdatabase_onDataChange - End");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "setupListeners - mdatabase_onCancelled - Start");


                Log.d(TAG, "setupListeners - mdatabase_onCancelled - End");
            }
        };
    }

    private void onTripAccepted() {
        TripDetailsFragment tripDetailsFragment = new TripDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("trip", Parcels.wrap(mtrip));
        tripDetailsFragment.setArguments(bundle);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.main_frame, tripDetailsFragment, tripDetailsFragment.TAG);
        fragmentTransaction.commit();
        dismiss();
        removeTripListener();
        requestStatus = RequestStatus.FINDING_LOCATION;
    }

    private void onTripNotServed() {
        onTripEnded();
    }

    private void onTripEnded() {
        dismiss();
        removeTripListener();
        requestStatus = RequestStatus.FINDING_LOCATION;
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
        switch (requestStatus) {
            case NOT_SERVED:
                onTripNotServed();
                break;
            case ACCEPTED:
                onTripAccepted();
                break;
            default:
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

    private static enum RequestStatus {FINDING_LOCATION, LOCATION_FOUND, LOCATION_NOT_FOUND, FINDING_DRIVERS, ACCEPTED, NOT_SERVED}

}
