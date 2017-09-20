package com.badeeb.driveit.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Amr Alghawy on 6/13/2017.
 */

public class JsonResponse<T> {

    @Expose(serialize = false, deserialize = true)
    @SerializedName("meta")
    private JsonMeta jsonMeta;

    @Expose(serialize = false, deserialize = true)
    @SerializedName("data")
    private T result;

    public JsonMeta getJsonMeta() {
        return jsonMeta;
    }

    public void setJsonMeta(JsonMeta jsonMeta) {
        this.jsonMeta = jsonMeta;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
