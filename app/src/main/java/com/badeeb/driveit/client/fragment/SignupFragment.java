package com.badeeb.driveit.client.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.badeeb.driveit.client.activity.MainActivity;
import com.badeeb.driveit.client.R;
import com.badeeb.driveit.client.model.JsonSignUp;
import com.badeeb.driveit.client.model.User;
import com.badeeb.driveit.client.network.MyVolley;
import com.badeeb.driveit.client.shared.AppPreferences;
import com.badeeb.driveit.client.shared.UiUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = SignupFragment.class.getSimpleName();

    // Class Attributes
//    private User user;

    private ProgressDialog progressDialog;

    // attributes that will be used for JSON calls
    private String url = AppPreferences.BASE_URL + "/client";

    //
    private static final int PERMISSION_READ_STORAGE = 145;
    private static final int IMAGE_GALLERY_REQUEST = 10;

    public SignupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        init(view);
        progressDialog = UiUtils.createProgressDialog(getActivity());



        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        // Attributes initialization
        MainActivity.mclient = new User();

        // Setup listeners
        setupListeners(view);

        Log.d(TAG, "init - End");
    }

    private void setupListeners(final View view) {
        Log.d(TAG, "setupListeners - Start");

        // Signup button listener
        Button signUpBttn = (Button) view.findViewById(R.id.sign_up);

        signUpBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View cview) {
                Log.d(TAG, "setupListeners - signUpBttn_onClick - Start");

                // Enable Progress bar
                progressDialog.show();

                EditText name = (EditText) view.findViewById(R.id.name);
                EditText email = (EditText) view.findViewById(R.id.email);
                EditText password = (EditText) view.findViewById(R.id.password);
                EditText phone = (EditText) view.findViewById(R.id.phone);
                RoundedImageView profileImage = (RoundedImageView) view.findViewById(R.id.profile_image);

                MainActivity.mclient.setName(name.getText().toString());
                MainActivity.mclient.setEmail(email.getText().toString());
                MainActivity.mclient.setPassword(password.getText().toString());
                MainActivity.mclient.setPhotoUrl("http://solarviews.com/raw/earth/earthafr.jpg"); // to be changed
                MainActivity.mclient.setPhoneNumber(phone.getText().toString());

                // Check signup using network call
                signup();

                Log.d(TAG, "setupListeners - signUpBttn_onClick - End");
            }
        });

        // Profile Image listener
        RoundedImageView profileImage = (RoundedImageView) view.findViewById(R.id.profile_image);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - profileImage_onClick - Start");

//                askForReadStoragePermission();

                Log.d(TAG, "setupListeners - profileImage_onClick - End");
            }
        });

        Log.d(TAG, "setupListeners - End");
    }

    private void signup() {
        Log.d(TAG, "signup - Start");

        try {

            JsonSignUp request = new JsonSignUp();
            request.setUser(MainActivity.mclient);

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "signup - Json Request"+ gson.toJson(request));

            // Call user login service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "signup - onResponse - Start");

                            Log.d(TAG, "signup - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonSignUp jsonResponse = gson.fromJson(responseData, JsonSignUp.class);

                            Log.d(TAG, "signup - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "signup - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                // Success login
                                // Move to next screen --> Main Activity
                                LoginFragment loginFragment = new LoginFragment();
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);

                                fragmentTransaction.addToBackStack(TAG);

                                fragmentTransaction.commit();
                            }
                            else {
                                // Invalid Signup
                                Toast.makeText(getContext(), getString(R.string.signup_error), Toast.LENGTH_LONG).show();
                            }

                            // Disable Progress bar
                            progressDialog.dismiss();

                            Log.d(TAG, "signup - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "signup - onErrorResponse: " + error.toString());


                            if (error instanceof ServerError) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                Log.d(TAG, "signup - Error Data: " + responseData);

                                JsonSignUp jsonResponse = gson.fromJson(responseData, JsonSignUp.class);

                                Log.d(TAG, "signup - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "signup - Error Message: " + jsonResponse.getJsonMeta().getMessage());

                                Toast.makeText(getContext(), jsonResponse.getJsonMeta().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            // Disable Progress bar
                            progressDialog.dismiss();
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
                    return headers;
                }
            };

            // Adding retry policy to request
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(AppPreferences.VOLLEY_TIME_OUT, AppPreferences.VOLLEY_RETRY_COUNTER, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            MyVolley.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "signup - End");
    }

//    private void askForReadStoragePermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//
//                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
//                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                    requestPermissions(
//                            new String[]
//                                    {Manifest.permission.READ_EXTERNAL_STORAGE}
//                            , PERMISSION_READ_STORAGE);
//                } else {
//
//
//                    /** MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an app-defined int constant. The callback method gets the result of the request. **/
//                    ActivityCompat.requestPermissions(getActivity(),
//                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                            PERMISSION_READ_STORAGE);
//                }
//            } else {
//                /** Already has permission */
//
//                openSelectPictureScreen();
//            }
//        } else {
//            /** No run time permission needed, version < M*/
//            openSelectPictureScreen();
//        }
//    }
//
//    private void openSelectPictureScreen() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_GALLERY_REQUEST);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (data != null && data.getData() != null && resultCode == RESULT_OK) {
//
//            Uri uri = data.getData();
//            RoundedImageView profileImage = (RoundedImageView) findViewById(R.id.profile_image);
//            profileImage.setImageURI(uri);
//
//            Log.d(TAG, "Image URI: "+uri);
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
}
