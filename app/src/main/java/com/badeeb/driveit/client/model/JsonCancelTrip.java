package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 9/19/2017.
 */

public class JsonCancelTrip {

    @Expose
    @SerializedName("trip_id")
    private int tripId;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("meta")
    private JsonMeta jsonMeta;

    public JsonCancelTrip() {
    }

    // Setters and Getters
    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public JsonMeta getJsonMeta() {
        return jsonMeta;
    }

    public void setJsonMeta(JsonMeta jsonMeta) {
        this.jsonMeta = jsonMeta;
    }
}
