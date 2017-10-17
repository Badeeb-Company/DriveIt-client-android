package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 10/18/2017.
 */

public class JsonUpdateAddress {

    @Expose(serialize = false, deserialize = true)
    @SerializedName("meta")
    private JsonMeta jsonMeta;

    @Expose(serialize = true, deserialize = false)
    @SerializedName("lat")
    private String lat;

    @Expose(serialize = true, deserialize = false)
    @SerializedName("long")
    private String lng;

    @Expose(serialize = true, deserialize = false)
    @SerializedName("address")
    private String address;

    public JsonUpdateAddress() {
    }

    public JsonMeta getJsonMeta() {
        return jsonMeta;
    }

    public void setJsonMeta(JsonMeta jsonMeta) {
        this.jsonMeta = jsonMeta;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
