package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 9/18/2017.
 */

public class JsonRequestTrip {

    @Expose(serialize = true, deserialize = false)
    @SerializedName("lat")
    private String lat;

    @Expose(serialize = true, deserialize = false)
    @SerializedName("long")
    private String lng;

    @Expose(serialize = true, deserialize = false)
    @SerializedName("destination")
    private String destination;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("meta")
    private JsonMeta jsonMeta;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("trip")
    private int tripId;

    public JsonRequestTrip() {
    }

    // Setters and Getters
    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public JsonMeta getJsonMeta() {
        return jsonMeta;
    }

    public void setJsonMeta(JsonMeta jsonMeta) {
        this.jsonMeta = jsonMeta;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }
}
