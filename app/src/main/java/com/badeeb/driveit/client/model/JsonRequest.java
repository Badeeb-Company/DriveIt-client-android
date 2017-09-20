package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 9/18/2017.
 */

public class JsonRequest<T> {

    @Expose (serialize = true, deserialize = false)
    @SerializedName("device")
    private Device device;

    @Expose (serialize = true, deserialize = false)
    @SerializedName("data")
    private T request;

    // Setters and Getters
    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }
}
