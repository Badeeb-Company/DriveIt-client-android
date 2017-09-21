package com.badeeb.driveit.client.shared;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by meldeeb on 9/21/17.
 */

public class FirebaseManager {

    public static final String CLIENTS_KEY = "clients";
    public static final String TRIP_KEY = "trip";

    private static DatabaseReference mDatabase;

    public FirebaseManager(){
        if(mDatabase == null){
            mDatabase = FirebaseDatabase.getInstance().getReference();
        }
    }

    public DatabaseReference createChildReference(String... keys){
        DatabaseReference result = mDatabase;
        for (String key: keys) {
            result = result.child(key);
        }
        return result;
    }


}
