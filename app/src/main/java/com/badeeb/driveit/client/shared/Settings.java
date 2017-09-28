package com.badeeb.driveit.client.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.badeeb.driveit.client.R;
import com.badeeb.driveit.client.controllers.DriveItApplication;
import com.badeeb.driveit.client.model.Trip;
import com.badeeb.driveit.client.model.User;

/**
 * Created by meldeeb on 9/25/17.
 */

public class Settings {

    private final static String PREF_USER_ID = "PREF_USER_ID";
    private final static String PREF_USER_MOBILE_NUMBER = "PREF_PHONE_NUMBER";
    private final static String PREF_USER_NAME = "PREF_USER_NAME";
    private final static String PREF_USER_EMAIL = "PREF_USER_EMAIL";
    private final static String PREF_USER_TOKEN = "PREF_USER_TOKEN";
    private final static String PREF_USER_IMAGE_URL = "PREF_USER_IMAGE_URL";

    private final static String PREF_TRIP_ID = "PREF_TRIP_ID";
    private final static String PREF_TRIP_DISTANCE_TO_ARRIVE = "PREF_TRIP_DISTANCE_TO_ARRIVE";
    private final static String PREF_TRIP_DRIVER_ADDRESS = "PREF_TRIP_DRIVER_ADDRESS";
    private final static String PREF_TRIP_DRIVER_ID = "PREF_TRIP_DRIVER_ID";
    private final static String PREF_TRIP_DRIVER_IMAGE_URL = "PREF_TRIP_DRIVER_IMAGE_URL";
    private final static String PREF_TRIP_DRIVER_LAT = "PREF_TRIP_DRIVER_LAT";
    private final static String PREF_TRIP_DRIVER_LONG = "PREF_TRIP_DRIVER_LONG";
    private final static String PREF_TRIP_DRIVER_NAME = "PREF_TRIP_DRIVER_NAME";
    private final static String PREF_TRIP_DRIVER_PHONE = "PREF_TRIP_DRIVER_PHONE";
    private final static String PREF_TRIP_TIME_TO_ARRIVE = "PREF_TRIP_TIME_TO_ARRIVE";

    private static Settings sInstance;

    private SharedPreferences sPreferences;

    public static Settings getInstance() {
        if (sInstance == null) {
            sInstance = new Settings(DriveItApplication.getInstance());
        }
        return sInstance;
    }


    private Settings(Context context) {
        String fileName = context.getString(R.string.app_name);
        this.sPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
    }

    private void putValue(String key, String value) {
        sPreferences.edit().putString(key, value).commit();
    }

    private String getValue(String key, String defaultValue) {
        return sPreferences.getString(key, defaultValue);
    }

    private void putValue(String key, int value) {
        sPreferences.edit().putInt(key, value).commit();
    }

    private int getValue(String key, int defaultValue) {
        return sPreferences.getInt(key, defaultValue);
    }

    private void putValue(String key, double value) {
        sPreferences.edit().putString(key, value+"").commit();
    }

    private double getValue(String key, double defaultValue) {
        return Double.parseDouble(sPreferences.getString(key, defaultValue+""));
    }

    public User getUser() {
        User user = new User();
        user.setId(getUserId());
        user.setName(getUserName());
        user.setEmail(getUserEmail());
        user.setPhoneNumber(getUserMobileNumber());
        user.setPhotoUrl(getUserImageUrl());
        user.setToken(getUserToken());
        return user;
    }

    public void saveUser(User user) {
        setUserId(user.getId());
        setUserMobileNumber(user.getPhoneNumber());
        setUserName(user.getName());
        setUserEmail(user.getEmail());
        setUserImageUrl(user.getPhotoUrl());
        setUserToken(user.getToken());
    }

    public void clearUserInfo() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(PREF_USER_ID)
                .remove(PREF_USER_EMAIL)
                .remove(PREF_USER_IMAGE_URL)
                .remove(PREF_USER_TOKEN)
                .remove(PREF_USER_NAME)
                .remove(PREF_USER_MOBILE_NUMBER);
        editor.commit();
    }

    public void saveTrip(Trip trip) {
        setTripId(trip.getId());
        setTripDistanceToArrive(trip.getDistance_to_arrive());
        setTripDriverAddress(trip.getDriver_address());
        setTripDriverId(trip.getDriver_id());
        setTripDriverImageURL(trip.getDriver_image_url());
        setTripDriverLat(trip.getDriver_lat());
        setTripDriverLong(trip.getDriver_long());
        setTripDriverName(trip.getDriver_name());
        setTripDriverPhone(trip.getDriver_phone());
        setTripTimeToArrive(trip.getTime_to_arrive());
    }

    public Trip getTrip() {
        Trip trip = new Trip();
        trip.setId(getTripId());
        trip.setDistance_to_arrive(getTripDistanceToArrive());
        trip.setDriver_address(getTripDriverAddress());
        trip.setDriver_id(getTripDriverId());
        trip.setDriver_image_url(getTripDriverImageURL());
        trip.setDriver_lat(getTripDriverLat());
        trip.setDriver_long(getTripDriverLong());
        trip.setDriver_name(getTripDriverName());
        trip.setDriver_phone(getTripDriverPhone());
        trip.setTime_to_arrive(getTripTimeToArrive());

        return trip;
    }

    public void clearTripInfo() {
        SharedPreferences.Editor editor = sPreferences.edit();
        editor.remove(PREF_TRIP_ID)
                .remove(PREF_TRIP_DISTANCE_TO_ARRIVE)
                .remove(PREF_TRIP_DRIVER_ADDRESS)
                .remove(PREF_TRIP_DRIVER_ID)
                .remove(PREF_TRIP_DRIVER_IMAGE_URL)
                .remove(PREF_TRIP_DRIVER_LAT)
                .remove(PREF_TRIP_DRIVER_LONG)
                .remove(PREF_TRIP_DRIVER_NAME)
                .remove(PREF_TRIP_DRIVER_PHONE)
                .remove(PREF_TRIP_TIME_TO_ARRIVE)
        ;
        editor.commit();
    }

    public boolean isLoggedIn() {
        String authenticationToken = getUserToken();
        return !TextUtils.isEmpty(authenticationToken);
    }

    public void setUserId(int userId) {
        putValue(PREF_USER_ID, userId);
    }

    public void setUserEmail(String prefEmail) {
        putValue(PREF_USER_EMAIL, prefEmail);
    }

    public void setUserName(String prefFirstName) {
        putValue(PREF_USER_NAME, prefFirstName);
    }

    public void setUserMobileNumber(String prefMobileNumber) {
        putValue(PREF_USER_MOBILE_NUMBER, prefMobileNumber);
    }

    public void setUserImageUrl(String prefUserImageUrl) {
        putValue(PREF_USER_IMAGE_URL, prefUserImageUrl);
    }

    public void setUserToken(String prefUserToken) {
        putValue(PREF_USER_TOKEN, prefUserToken);
    }

    public int getUserId() {
        return getValue(PREF_USER_ID, 0);
    }

    public String getUserToken() {
        return getValue(PREF_USER_TOKEN, "");
    }

    public String getUserEmail() {
        return getValue(PREF_USER_EMAIL, "");
    }

    public String getUserName() {
        return getValue(PREF_USER_NAME, "");
    }

    public String getUserMobileNumber() {
        return getValue(PREF_USER_MOBILE_NUMBER, "");
    }

    public String getUserImageUrl() {
        return getValue(PREF_USER_IMAGE_URL, "");
    }

    public void setTripId(int prefTripId) {
        putValue(PREF_TRIP_ID, prefTripId);
    }

    public int getTripId() {
        return getValue(PREF_TRIP_ID, 0);
    }

    public void setTripDistanceToArrive(double prefTripDistanceToArrive) {
        putValue(PREF_TRIP_DISTANCE_TO_ARRIVE, prefTripDistanceToArrive);
    }

    public double getTripDistanceToArrive() {
        return getValue(PREF_TRIP_DISTANCE_TO_ARRIVE, 0.0);
    }

    public void setTripDriverAddress(String prefTripDriverAddress) {
        putValue(PREF_TRIP_DRIVER_ADDRESS, prefTripDriverAddress);
    }

    public String getTripDriverAddress() {
        return getValue(PREF_TRIP_DRIVER_ADDRESS, "");
    }

    public void setTripDriverId(int prefTripDriverId) {
        putValue(PREF_TRIP_DRIVER_ID, prefTripDriverId);
    }

    public int getTripDriverId() {
        return getValue(PREF_TRIP_DRIVER_ID, 0);
    }

    public void setTripDriverImageURL(String prefTripDriverImageURL) {
        putValue(PREF_TRIP_DRIVER_IMAGE_URL, prefTripDriverImageURL);
    }

    public String getTripDriverImageURL() {
        return getValue(PREF_TRIP_DRIVER_IMAGE_URL, "");
    }

    public void setTripDriverLat(double prefTripDriverLat) {
        putValue(PREF_TRIP_DRIVER_LAT, prefTripDriverLat);
    }

    public double getTripDriverLat() {
        return getValue(PREF_TRIP_DRIVER_LAT, 0.0);
    }

    public void setTripDriverLong(double prefTripDriverLong) {
        putValue(PREF_TRIP_DRIVER_LONG, prefTripDriverLong);
    }

    public double getTripDriverLong() {
        return getValue(PREF_TRIP_DRIVER_LONG, 0.0);
    }

    public void setTripDriverName(String prefTripDriverName) {
        putValue(PREF_TRIP_DRIVER_NAME, prefTripDriverName);
    }

    public String getTripDriverName() {
        return getValue(PREF_TRIP_DRIVER_NAME, "");
    }

    public void setTripDriverPhone(String prefTripDriverPhone) {
        putValue(PREF_TRIP_DRIVER_PHONE, prefTripDriverPhone);
    }

    public String getTripDriverPhone() {
        return getValue(PREF_TRIP_DRIVER_PHONE, "");
    }


    public void setTripTimeToArrive(double prefTripTimeToArrive) {
        putValue(PREF_TRIP_TIME_TO_ARRIVE, prefTripTimeToArrive);
    }

    public double getTripTimeToArrive() {
        return getValue(PREF_TRIP_TIME_TO_ARRIVE, 0.0);
    }


}

