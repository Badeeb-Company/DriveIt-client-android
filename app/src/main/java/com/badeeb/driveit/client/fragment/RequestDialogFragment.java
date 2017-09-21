package com.badeeb.driveit.client.fragment;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
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
import com.badeeb.driveit.client.model.FDBTrip;
import com.badeeb.driveit.client.model.JsonCancelTrip;
import com.badeeb.driveit.client.model.JsonLogin;
import com.badeeb.driveit.client.model.Trip;
import com.badeeb.driveit.client.model.User;
import com.badeeb.driveit.client.network.MyVolley;
import com.badeeb.driveit.client.shared.AppPreferences;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link DialogFragment} subclass.
 */
public class RequestDialogFragment extends DialogFragment {

    // Logging Purpose
    public static final String TAG = RequestDialogFragment.class.getSimpleName();

    // attributes that will be used for JSON calls
    private String url = AppPreferences.BASE_URL + "/trip";

    // Class Attributes
//    private User client;
    private Trip mtrip;
    private ProgressBar mProgressBar;

    // Firebase database reference
    private DatabaseReference mDatabase;

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
        Log.d(TAG, "init - Start");

        // Get client object from extra
//        this.client = Parcels.unwrap(getArguments().getParcelable("client"));
        this.mtrip = Parcels.unwrap(getArguments().getParcelable("trip"));

        mProgressBar = view.findViewById(R.id.progressBar);

        // Initiate firebase realtime - database
        this.mDatabase = FirebaseDatabase.getInstance().getReference();

        // Setup Listeners
        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    public void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        final Button cancelRide = (Button) view.findViewById(R.id.cancel_ride);
        cancelRide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - cancelRide_onClick - Start");

                // Stop firebase database listener
                removeFDBListener();


                // Send cancel request
                cancelRide();

                Log.d(TAG, "setupListeners - cancelRide_onClick - End");
            }
        });


        // Create listener on firebase realtime - database
        this.mDatabase.child("clients").child(MainActivity.mclient.getId()+"").child("trip").addValueEventListener(new ValueEventListener() {
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
                        fdbTrip.setLng(mtrip.getLng());
                        fdbTrip.setDestination(mtrip.getDestination());
                        fdbTrip.setClientId(mtrip.getClientId());

                        mtrip = fdbTrip;

                        TripDetailsFragment tripDetailsFragment = new TripDetailsFragment();
                        Bundle bundle = new Bundle();
//                        bundle.putParcelable("client", Parcels.wrap(client));
                        bundle.putParcelable("trip", Parcels.wrap(mtrip));
                        tripDetailsFragment.setArguments(bundle);

                        FragmentManager fragmentManager = getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                        fragmentTransaction.add(R.id.main_frame, tripDetailsFragment, tripDetailsFragment.TAG);

                        fragmentTransaction.addToBackStack(TAG);

                        fragmentTransaction.commit();
                    }
                    else {
                        // Trip rejected
                        Toast.makeText(getContext(), R.string.request_again, Toast.LENGTH_SHORT).show();
                    }

                    // Remove firebase database listener
                    removeFDBListener();

                    RequestDialogFragment.this.dismiss();
                }

                Log.d(TAG, "setupListeners - mdatabase_onDataChange - End");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "setupListeners - mdatabase_onCancelled - Start");


                Log.d(TAG, "setupListeners - mdatabase_onCancelled - End");
            }
        });

        Log.d(TAG, "setupListeners - End");
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

            Log.d(TAG, "cancelRide - Json Request"+ gson.toJson(request));

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

                            }
                            else {
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

        RequestDialogFragment.this.dismiss();

        Log.d(TAG, "cancelRide - End");
    }

    private void removeFDBListener() {
        Log.d(TAG, "removeFDBListener - Start");

        Log.d(TAG, "removeFDBListener - End");
    }

}
