package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 9/18/2017.
 */

public class JsonSignUp {

    @Expose(serialize = false, deserialize = true)
    @SerializedName("meta")
    private JsonMeta jsonMeta;

    @Expose
    @SerializedName("client")
    private User user;

    // Setters and Getters
    public JsonMeta getJsonMeta() {
        return jsonMeta;
    }

    public void setJsonMeta(JsonMeta jsonMeta) {
        this.jsonMeta = jsonMeta;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
