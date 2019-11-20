package com.sgo.saldomu.models.retrofit;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CommDataModel extends jsonModel{
    @SerializedName("community")
    @Expose
    private
    JsonArray community;

    public JsonArray getCommunity() {
        return community;
    }
}
