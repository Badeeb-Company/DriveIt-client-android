package com.badeeb.driveit.client.fragment;


import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
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
import com.badeeb.driveit.client.shared.OnPermissionsGrantedHandler;
import com.badeeb.driveit.client.shared.PermissionsChecker;
import com.badeeb.driveit.client.shared.UiUtils;
import com.badeeb.driveit.client.shared.Utils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.badeeb.driveit.client.shared.Utils.getBytes;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignupFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = SignupFragment.class.getSimpleName();
    private static final int PERM_LOCATION_RQST_CODE = 200;
    private static final int IMAGE_GALLERY_REQUEST = 10;

    // Class Attributes

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private ProgressDialog progressDialog;
    private Button bSelectPhoto;
    private RoundedImageView rivProfilePhoto;
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText phone;
    // attributes that will be used for JSON calls
    private String url = AppPreferences.BASE_URL + "/client";

    private User mClient;
    private MainActivity mactivity;
    private Uri photoUri;
    private String uploadedPhotoUrl;
    private boolean photoChosen;

    private OnPermissionsGrantedHandler onStoragePermissionGrantedHandler;

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

        Log.d(TAG, "onCreateView - End");
        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

        // Attributes initialization
        mClient = new User();
        mactivity = (MainActivity) getActivity();
        onStoragePermissionGrantedHandler = createOnStoragePermissionGrantedHandler();

        progressDialog = UiUtils.createProgressDialog(getActivity(), "Signing up...");
        bSelectPhoto = (Button) view.findViewById(R.id.bSelectPhoto);
        rivProfilePhoto = (RoundedImageView) view.findViewById(R.id.rivProfilePhoto);
        name = (EditText) view.findViewById(R.id.name);
        email = (EditText) view.findViewById(R.id.email);
        password = (EditText) view.findViewById(R.id.password);
        phone = (EditText) view.findViewById(R.id.phone);


        // Setup listeners
        setupListeners(view);

        // Refresh menu toolbar
        ((MainActivity) getActivity()).disbleNavigationView();

        Log.d(TAG, "init - End");
    }

    private void uploadToFirebase() {
        InputStream inputStream = null;
        try {
            inputStream = mactivity.getContentResolver().openInputStream(photoUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] inputData = Utils.getBytes(inputStream);

        StorageReference storageRef = storage.getReference();
        StorageReference imageReference = storageRef.child("clients/" + UUID.randomUUID());
        final ProgressDialog uploadPhotoProgressDialog = UiUtils.createProgressDialog(mactivity, "Uploading photo...");

        uploadPhotoProgressDialog.show();

        UploadTask uploadTask = imageReference.putBytes(inputData);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(mactivity, "Cannot upload photo, please choose another one.", Toast.LENGTH_LONG).show();
                uploadPhotoProgressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                uploadedPhotoUrl = downloadUrl.toString();
                uploadPhotoProgressDialog.dismiss();
                photoChosen = false;
                callSignup();
            }
        });
    }

    private OnPermissionsGrantedHandler createOnStoragePermissionGrantedHandler() {
        return new OnPermissionsGrantedHandler() {
            @Override
            public void onPermissionsGranted() {
                openSelectPhotoScreen();
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERM_LOCATION_RQST_CODE: {
                if (PermissionsChecker.permissionsGranted(grantResults)) {
                    onStoragePermissionGrantedHandler.onPermissionsGranted();
                }
            }
        }
    }

    private void openSelectPhotoScreen() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Photo"), IMAGE_GALLERY_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(mactivity, "Cannot select this photo, please choose another one", Toast.LENGTH_SHORT).show();
                return;
            }
            photoChosen = true;
            photoUri = data.getData();
            Glide.with(mactivity)
                    .load(photoUri)
                    .into(rivProfilePhoto);
        }
    }

    private void setupListeners(final View view) {
        Log.d(TAG, "setupListeners - Start");

        // Signup button listener
        Button signUpBttn = (Button) view.findViewById(R.id.sign_up);

        signUpBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View cview) {
                if (! validateInput()) {
                    return;
                }

                if (photoUri == null) {
                    Toast.makeText(mactivity, "Please select profile photo to continue", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (photoChosen) {
                    if(Utils.isAllowedFileSize(mactivity, photoUri)) {
                        uploadToFirebase();
                    }
                } else {
                    callSignup();
                }

            }
        });

        bSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionsChecker.checkPermissions(SignupFragment.this, onStoragePermissionGrantedHandler,
                        PERM_LOCATION_RQST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });


        Log.d(TAG, "setupListeners - End");
    }

    private boolean validateInput() {

        boolean valid = true;

        if (email.getText().toString().isEmpty()) {
            // Empty Email
            email.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (! AppPreferences.isEmailValid(email.getText().toString())) {
            // Email is wrong
            email.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (password.getText().toString().isEmpty()) {
            // Empty Password
            password.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (! AppPreferences.isPasswordValid(password.getText().toString())) {
            password.setError(getString(R.string.error_invalid_password));
            valid = false;
        }

        if (name.getText().toString().isEmpty()) {
            // Empty name
            name.setError(getString(R.string.error_field_required));
            valid = false;
        }

        if (phone.getText().toString().isEmpty()) {
            // Empty Phone
            phone.setError(getString(R.string.error_field_required));
            valid = false;
        }
        else if (! AppPreferences.isPhoneNumberValid(phone.getText().toString())) {
            phone.setError(getString(R.string.error_invalid_phone_number));
            valid = false;
        }

        return valid;
    }


    private void goToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_frame, loginFragment, loginFragment.TAG);
        fragmentTransaction.addToBackStack(TAG);
        fragmentTransaction.commit();
    }

    private void callSignup() {
        progressDialog.show();
        mClient.setName(name.getText().toString());
        mClient.setEmail(email.getText().toString());
        mClient.setPassword(password.getText().toString());
        mClient.setPhotoUrl(uploadedPhotoUrl); // to be changed
        mClient.setPhoneNumber(phone.getText().toString());
        try {
            JsonSignUp request = new JsonSignUp();
            request.setUser(mClient);

            // Create Gson object
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.excludeFieldsWithoutExposeAnnotation();
            final Gson gson = gsonBuilder.create();

            JSONObject jsonObject = new JSONObject(gson.toJson(request));

            Log.d(TAG, "callSignup - Json Request" + gson.toJson(request));

            // Call mClient login service
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,

                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            // Response Handling
                            Log.d(TAG, "callSignup - onResponse - Start");

                            Log.d(TAG, "callSignup - onResponse - Json Response: " + response.toString());

                            String responseData = response.toString();

                            JsonSignUp jsonResponse = gson.fromJson(responseData, JsonSignUp.class);

                            Log.d(TAG, "callSignup - onResponse - Status: " + jsonResponse.getJsonMeta().getStatus());
                            Log.d(TAG, "callSignup - onResponse - Message: " + jsonResponse.getJsonMeta().getMessage());

                            // check status  code of response
                            if (jsonResponse.getJsonMeta().getStatus().equals("200")) {
                                UiUtils.showDialog(getContext(), R.style.DialogTheme, R.string.success_sign_up, R.string.ok_btn_dialog, null);

                                goToLogin();
                            } else {
                                // Invalid Signup
                                Toast.makeText(getContext(), getString(R.string.signup_error), Toast.LENGTH_LONG).show();
                            }

                            // Disable Progress bar
                            progressDialog.dismiss();

                            Log.d(TAG, "callSignup - onResponse - End");
                        }
                    },

                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Network Error Handling
                            Log.d(TAG, "callSignup - onErrorResponse: " + error.toString());


                            if (error instanceof ServerError) {
                                NetworkResponse response = error.networkResponse;
                                String responseData = new String(response.data);

                                Log.d(TAG, "callSignup - Error Data: " + responseData);

                                JsonSignUp jsonResponse = gson.fromJson(responseData, JsonSignUp.class);

                                Log.d(TAG, "callSignup - Error Status: " + jsonResponse.getJsonMeta().getStatus());
                                Log.d(TAG, "callSignup - Error Message: " + jsonResponse.getJsonMeta().getMessage());

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

        Log.d(TAG, "callSignup - End");
    }

}
