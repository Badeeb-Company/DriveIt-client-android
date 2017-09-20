package com.badeeb.driveit.client;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.badeeb.driveit.client.fragment.LoginFragment;
import com.badeeb.driveit.client.fragment.TripRequestFragment;
import com.badeeb.driveit.client.model.JsonLogin;
import com.badeeb.driveit.client.model.JsonLogout;
import com.badeeb.driveit.client.model.User;
import com.badeeb.driveit.client.network.MyVolley;
import com.badeeb.driveit.client.shared.AppPreferences;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Logging Purpose
    private final String TAG = MainActivity.class.getSimpleName();

    // Class attributes
    private Toolbar mtoolbar;
    private FragmentManager mFragmentManager;
    private MenuItem mlogoutItem;

    public static User mclient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate - Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        Log.d(TAG, "onCreate - End");
    }

    private void init() {
        Log.d(TAG, "init - Start");

        // Initialize Attributes
        mFragmentManager = getSupportFragmentManager();

        // Toolbar
        this.mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(this.mtoolbar);


        // Load Login Fragment inside Main activity
        // Fragment creation
        LoginFragment loginFragment = new LoginFragment();

        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);

        fragmentTransaction.commit();


        Log.d(TAG, "init - End");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d(TAG, "onCreateOptionsMenu - Start");

        getMenuInflater().inflate(R.menu.menu, menu);

        mlogoutItem = menu.findItem(R.id.nav_logout);

        mlogoutItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.d(TAG, "setupListeners - mlogoutItem_onMenuItemClick - Start");

                logout();

                Log.d(TAG, "setupListeners - mlogoutItem_onMenuItemClick - End");
                return false;
            }
        });

        Log.d(TAG, "onCreateOptionsMenu - End");
        return true;
    }


    private void logout() {
        Log.d(TAG, "logout - Start");

        String url = AppPreferences.BASE_URL + "/logout";

        try {

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            // Call user login service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "logout - onResponse - Start");

                            Log.d(TAG, "logout - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonLogout jsonResponse = gson.fromJson(responseData, JsonLogout.class);

                            Log.d(TAG, "logout - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "logout - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            // Success login
                            // Clear callback stack
                            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                            // Move to next screen --> Login fragment
                            LoginFragment loginFragment = new LoginFragment();

                            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

                            fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);

                            fragmentTransaction.commit();



                            Log.d(TAG, "logout - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "logout - onErrorResponse: " + error.toString());

                            if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                JsonLogin jsonResponse = gson.fromJson(responseData, JsonLogin.class);

                                Log.d(TAG, "logout - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "logout - Error Message: " + jsonResponse.getJsonMeta().getMessage());

                                Toast.makeText(getApplicationContext(), jsonResponse.getJsonMeta().getMessage(), Toast.LENGTH_SHORT).show();
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

            MyVolley.getInstance(this).addToRequestQueue(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(TAG, "logout - End");
    }
}
