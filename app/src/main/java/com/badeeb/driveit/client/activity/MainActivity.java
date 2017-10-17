package com.badeeb.driveit.client.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
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
import com.badeeb.driveit.client.fragment.LoginFragment;
import com.badeeb.driveit.client.fragment.TripRequestFragment;
import com.badeeb.driveit.client.fragment.UpdateAddressFragment;
import com.badeeb.driveit.client.model.JsonLogin;
import com.badeeb.driveit.client.model.JsonLogout;
import com.badeeb.driveit.client.model.User;
import com.badeeb.driveit.client.network.MyVolley;
import com.badeeb.driveit.client.shared.AppPreferences;
import com.badeeb.driveit.client.shared.AppSettings;
import com.badeeb.driveit.client.shared.UiUtils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Logging Purpose
    private final String TAG = MainActivity.class.getSimpleName();

    // Class attributes
    private Toolbar mtoolbar;
    private FragmentManager mFragmentManager;
    private DrawerLayout mdrawer;
    private ActionBarDrawerToggle mtoggle;
    private NavigationView mnavigationView;
    private AppSettings msettings;

    private User mclient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate - Start");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        init();

        Log.d(TAG, "onCreate - End");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        Log.d(TAG, "onNavigationItemSelected - Start");

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            // Handle the logout action
            Log.d(TAG, "onNavigationItemSelected - Logout - Start");
            msettings.clearUserInfo();
            goToLogin();
        }
        else if (id == R.id.nav_address) {
            goToUpdateAddress();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        Log.d(TAG, "onNavigationItemSelected - End");

        return true;
    }

    private void init() {
        Log.d(TAG, "init - Start");

        // Initialize Attributes
        mFragmentManager = getSupportFragmentManager();
        msettings = AppSettings.getInstance();

        // Toolbar
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);


        mdrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mtoggle = new ActionBarDrawerToggle(
                this, mdrawer, mtoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mdrawer.setDrawerListener(mtoggle);
        mtoggle.syncState();

        mnavigationView = (NavigationView) findViewById(R.id.nav_view);
        mnavigationView.setNavigationItemSelectedListener(this);

        // Load Login Fragment inside Main activity
        // Load Login Fragment
        if (msettings.isLoggedIn()) {
            mclient = msettings.getUser();
            setNavigationViewValues(mclient);
            TripRequestFragment tripRequestFragment = new TripRequestFragment();
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.main_frame, tripRequestFragment, tripRequestFragment.TAG);
            fragmentTransaction.commit();
        } else {
            goToLogin();
        }


        Log.d(TAG, "init - End");
    }

    private void goToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.commit();
    }

    private void goToUpdateAddress() {
        UpdateAddressFragment updateAddressFragment = new UpdateAddressFragment();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, updateAddressFragment, updateAddressFragment.TAG);
        fragmentTransaction.addToBackStack(updateAddressFragment.TAG);
        fragmentTransaction.commit();
    }

    public void disbleNavigationView() {
        mdrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mtoggle.setDrawerIndicatorEnabled(false);
    }

    public void enbleNavigationView() {
        mdrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mtoggle.setDrawerIndicatorEnabled(true);
    }

    public void setNavigationViewValues(User client) {
        View view = mnavigationView.getHeaderView(0);
        RoundedImageView profilePhoto = (RoundedImageView) view.findViewById(R.id.rivProfilePhoto);
        TextView tvProfileName = (TextView) view.findViewById(R.id.tv_profile_name);
        TextView tvProfileEmail = (TextView) view.findViewById(R.id.tv_profile_email);
        tvProfileName.setText(client.getName());
        tvProfileEmail.setText(client.getEmail());
        Glide.with(this)
                .load(client.getPhotoUrl())
                .into(profilePhoto);
    }

    public void setClient(User client) {
        mclient = client;
    }

    public User getClient() {
        return mclient;
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

                            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                                // Authorization issue
                                UiUtils.showDialog(getApplicationContext(), R.style.DialogTheme, R.string.account_not_active, R.string.ok_btn_dialog, null);

                            } else if (error instanceof ServerError && error.networkResponse.statusCode != 404) {
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
                    headers.put("Authorization", "Token token=" + MainActivity.this.mclient.getToken());
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
