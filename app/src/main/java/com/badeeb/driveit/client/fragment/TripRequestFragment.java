package com.badeeb.driveit.client.fragment;


import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import com.badeeb.driveit.client.MainActivity;
import com.badeeb.driveit.client.R;
import com.badeeb.driveit.client.model.JsonRequestTrip;
import com.badeeb.driveit.client.model.Trip;
import com.badeeb.driveit.client.model.User;
import com.badeeb.driveit.client.network.MyVolley;
import com.badeeb.driveit.client.shared.AppPreferences;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class TripRequestFragment extends Fragment implements LocationListener {

    // Logging Purpose
    public static final String TAG = TripRequestFragment.class.getSimpleName();

    // attributes that will be used for JSON calls
    private String url = AppPreferences.BASE_URL + "/trip";

    // Class Attributes
//    private User client;
    private Location mCurrentLocation;
    private String mCurrentAddress;

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

        // Get client object from extra
//        this.client = Parcels.unwrap(getArguments().getParcelable("client"));

        // Setup Listeners
        setupListeners(view);

        // Refresh menu toolbar
        setHasOptionsMenu(true);

        Log.d(TAG, "init - End");
    }

    public void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        // Request Truck button
        final Button requestTruck = (Button) view.findViewById(R.id.request_truck);

        requestTruck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - requestTruck_onclick - Start");

                // Get current location
                mCurrentLocation = getCurrentLocation();


                if (mCurrentLocation != null) {

                    // Get current Address string
                    mCurrentAddress = getCurrentAddress(mCurrentLocation);

                    // Send Request truck to server
                    requestTruck();
                }

                Log.d(TAG, "setupListeners - requestTruck_onclick - End");
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void requestTruck() {
        Log.d(TAG, "requestTruck - Start");

//        FragmentManager fragmentManager = getFragmentManager();
//
//        RequestDialogFragment requestDialogFragment = new RequestDialogFragment();
//        Bundle bundle = new Bundle();
//        bundle.putParcelable("client", Parcels.wrap(client));
//        requestDialogFragment.setArguments(bundle);
//
//        requestDialogFragment.setCancelable(false);
//        requestDialogFragment.show(fragmentManager, requestDialogFragment.TAG);


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

            Log.d(TAG, "requestTruck - Json Request"+ gson.toJson(request));

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

                                Trip trip = new Trip();
                                trip.setId(jsonResponse.getTripId());
                                trip.setDestination(mCurrentAddress);
                                trip.setLng(mCurrentLocation.getLongitude());
                                trip.setLat(mCurrentLocation.getLatitude());
                                trip.setClientId(MainActivity.mclient.getId());

                                RequestDialogFragment requestDialogFragment = new RequestDialogFragment();
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("trip", Parcels.wrap(trip));
                                requestDialogFragment.setArguments(bundle);
                                requestDialogFragment.setCancelable(false);

                                FragmentManager fragmentManager = getFragmentManager();
                                requestDialogFragment.show(fragmentManager, requestDialogFragment.TAG);
                            }
                            else {
                                // Invalid login
                                Toast.makeText(getContext(), getString(R.string.try_error), Toast.LENGTH_SHORT).show();
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
                    headers.put("Authorization", "Token token=" + MainActivity.mclient.getToken());

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

    // Current Location Inquiry
    private Location getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation - Start");

        Location currentLocation = null;
        // Check if GPS is granted or not
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted now
            Log.d(TAG, "getCurrentLocation - Permission is granted");

            LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (isGPSEnabled || isNetworkEnabled) {
                List<String> providers = locationManager.getProviders(true);
                for (String provider : providers) {
                    locationManager.requestLocationUpdates(provider, 0, 0, this);
                    Location l = locationManager.getLastKnownLocation(provider);
                    if (l == null) {
                        continue;
                    }
                    if (currentLocation == null || l.getAccuracy() < currentLocation.getAccuracy()) {
                        // Found best last known location: %s", l);
                        currentLocation = l;
                    }
                }
                if (currentLocation == null) {
                    Log.d(TAG, "getCurrentLocation - CurrentLocation is null");
                    Toast.makeText(getContext(), getResources().getText(R.string.enable_gps), Toast.LENGTH_SHORT).show();
                }

            } else {
                // Error
                Log.d(TAG, "getCurrentLocation - GPS and Network are not enabled");

                // Show Alert to enable GPS
                Toast.makeText(getContext(), getResources().getText(R.string.enable_gps), Toast.LENGTH_SHORT).show();

                return null;
            }

        } else {
            // Location permission is required
            Log.d(TAG, "getCurrentLocation - Permission is not granted");

            // Show Alert to enable location service
            Toast.makeText(getContext(), "Grant location permission to APP", Toast.LENGTH_SHORT).show();

            // TODO: Show dialog to grant permissions and reload data after that
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }

        Log.d(TAG, "getCurrentLocation - End");

        return currentLocation;
    }

    private String getCurrentAddress(Location location) {
        Log.d(TAG, "getCurrentAddress - Start");

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        String address = "";
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            address = addresses.get(0).getAddressLine(0) + ", "
                    + addresses.get(0).getAddressLine(1) + ", "
                    + addresses.get(0).getAddressLine(2) + ", "
                    + addresses.get(0).getAddressLine(3) + ", "
                    + addresses.get(0).getAddressLine(4)
            ;
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "getCurrentAddress - End");

        return address;
    }

    // ---------------------------------------------------------------
    // Location interface methods
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged - Start");
        Log.d(TAG, "onLocationChanged - End");
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


}
