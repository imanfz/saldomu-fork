package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataReqModel extends jsonModel {
    @SerializedName("product_value")
    @Expose
    private
    String product_value;

    public String getProduct_value() {
        return product_value;
    }
}
