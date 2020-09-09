package com.sgo.saldomu.models.retrofit;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BillerModel extends jsonModel {
    @SerializedName("biller_data")
    @Expose
    private
    JsonArray biller_data;
//    List<BillerDataModel> biller_data;

//    public List<BillerDataModel> getBiller_data() {
    public JsonArray getBiller_data() {
        return biller_data;
    }
}
