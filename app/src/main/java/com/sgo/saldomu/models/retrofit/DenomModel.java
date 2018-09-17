package com.sgo.saldomu.models.retrofit;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DenomModel extends jsonModel {
    @SerializedName("denom_data")
    @Expose
    private
    JsonArray denom_data;

    public JsonArray getDenom_data() {
        return denom_data;
    }
}
