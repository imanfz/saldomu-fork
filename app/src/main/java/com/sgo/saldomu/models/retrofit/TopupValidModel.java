package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TopupValidModel extends jsonModel {
    @SerializedName("tx_id")
    @Expose
    private
    String tx_id;
    @SerializedName("product_code")
    @Expose
    private
    String product_code;
    @SerializedName("comm_code")
    @Expose
    private
    String comm_code;
    @SerializedName("fee")
    @Expose
    private
    String fee;

    public String getTx_id() {
        return tx_id;
    }

    public String getProduct_code() {
        return product_code;
    }

    public String getComm_code() {
        return comm_code;
    }

    public String getFee() {
        return fee;
    }
}
