package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;

/**
 * Created by Amr Alghawy on 9/18/2017.
 */

public class Device {

    // Class Attributes
    @Expose
    private int id;
    @Expose
    private String fcmToken;
    @Expose
    private String deviceType;

    // Constructor
    public Device() {
        this.id = 0;
        this.fcmToken = "";
        this.deviceType = "";
    }

    // Setters and Getters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
}
