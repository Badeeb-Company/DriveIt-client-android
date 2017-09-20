package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;

/**
 * Created by Amr Alghawy on 6/13/2017.
 */

public class JsonMeta {
    @Expose(serialize = false, deserialize = true)
    private String status;

    @Expose(serialize = false, deserialize = true)
    private String message;

    // Constructor
    public JsonMeta() {
        this.status = "";
        this.message = "";
    }

    // Setters and Getters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
