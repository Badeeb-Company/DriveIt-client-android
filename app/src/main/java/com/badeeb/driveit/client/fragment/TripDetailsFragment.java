package com.badeeb.driveit.client.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.badeeb.driveit.client.R;
import com.badeeb.driveit.client.activity.MainActivity;
import com.badeeb.driveit.client.model.Trip;
import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;

import org.parceler.Parcels;

/**
 * A simple {@link Fragment} subclass.
 */
public class TripDetailsFragment extends Fragment {

    // Logging Purpose
    public static final String TAG = TripDetailsFragment.class.getSimpleName();

    private Trip mtrip;

    public TripDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Start");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_trip_details, container, false);

        init(view);

        Log.d(TAG, "onCreateView - End");

        return view;
    }

    private void init(View view) {
        Log.d(TAG, "init - Start");

//        this.mclient = Parcels.unwrap(getArguments().getParcelable("client"));
        this.mtrip = Parcels.unwrap(getArguments().getParcelable("trip"));

        // Initialize text fields with their correct values
        RoundedImageView driverPhoto = view.findViewById(R.id.iProfileImage);
        TextView driverName = view.findViewById(R.id.tDriverName);
        TextView driverPhone = view.findViewById(R.id.tdriverPhone);
        TextView driverTimeToArrive = view.findViewById(R.id.tTimeToArrive);
        TextView driverDistance = view.findViewById(R.id.tDriverDistance);
        TextView tvVehicleType = view.findViewById(R.id.tvVehicleType);
        TextView tvRestart = view.findViewById(R.id.tvRestart);

        Glide.with(getContext())
                .load(mtrip.getDriver_image_url())
                .into(driverPhoto);

        driverName.setText(mtrip.getDriver_name());
        driverPhone.setText(mtrip.getDriver_phone());
        driverTimeToArrive.setText((int)(mtrip.getTime_to_arrive()/60) + " minutes");
        driverDistance.setText(mtrip.getDistance_to_arrive()/1000 + " kilometers");
        tvVehicleType.setText(mtrip.getDriver_type());

        tvRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TripRequestFragment tripRequestFragment = new TripRequestFragment();
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.main_frame, tripRequestFragment, tripRequestFragment.TAG);
                fragmentTransaction.commit();
            }
        });

        // Setup Listeners
        setupListeners(view);

        // Refresh menu toolbar
        ((MainActivity) getActivity()).enbleNavigationView();

        Log.d(TAG, "init - End");
    }

    public void setupListeners(View view) {
        Log.d(TAG, "setupListeners - Start");

        Button bCall = view.findViewById(R.id.bCall);

        bCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "setupListeners - bCall_setOnClickListener - Start");

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                String mobileNumber = String.valueOf(mtrip.getDriver_phone());
                callIntent.setData(Uri.parse("tel:" + mobileNumber));

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // Show Alert to enable location service
                    Toast.makeText(getContext(), getContext().getResources().getText(R.string.enable_phone_call), Toast.LENGTH_SHORT).show();

                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 0);
                }else {
                    getContext().startActivity(callIntent);
                }

                Log.d(TAG, "setupListeners - bCall_setOnClickListener - End");
            }
        });

        Log.d(TAG, "setupListeners - End");
    }
}
