package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 9/20/2017.
 */

public class FDBTrip {

    private int id;
    private String state;
    private double distance_to_arrive;
    private String driver_address;
    private int driver_id;
    private String driver_image_url;
    private double driver_lat;
    private double driver_long;
    private String driver_name;
    private String driver_phone;
    private double time_to_arrive;

    public FDBTrip() {
    }

    // Setters and Getters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public double getDistance_to_arrive() {
        return distance_to_arrive;
    }

    public void setDistance_to_arrive(double distance_to_arrive) {
        this.distance_to_arrive = distance_to_arrive;
    }

    public String getDriver_address() {
        return driver_address;
    }

    public void setDriver_address(String driver_address) {
        this.driver_address = driver_address;
    }

    public int getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(int driver_id) {
        this.driver_id = driver_id;
    }

    public String getDriver_image_url() {
        return driver_image_url;
    }

    public void setDriver_image_url(String driver_image_url) {
        this.driver_image_url = driver_image_url;
    }

    public double getDriver_lat() {
        return driver_lat;
    }

    public void setDriver_lat(double driver_lat) {
        this.driver_lat = driver_lat;
    }

    public double getDriver_long() {
        return driver_long;
    }

    public void setDriver_long(double driver_long) {
        this.driver_long = driver_long;
    }

    public String getDriver_name() {
        return driver_name;
    }

    public void setDriver_name(String driver_name) {
        this.driver_name = driver_name;
    }

    public String getDriver_phone() {
        return driver_phone;
    }

    public void setDriver_phone(String driver_phone) {
        this.driver_phone = driver_phone;
    }

    public double getTime_to_arrive() {
        return time_to_arrive;
    }

    public void setTime_to_arrive(double time_to_arrive) {
        this.time_to_arrive = time_to_arrive;
    }
}
