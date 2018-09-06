package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PayFriendConfirmModel extends jsonModel {
    @SerializedName("data")
    @Expose
    private
    List<PayfriendDataModel> data;
    @SerializedName("buss_scheme_code")
    @Expose
    private
    String buss_scheme_code;
    @SerializedName("buss_scheme_name")
    @Expose
    private
    String buss_scheme_name;
    @SerializedName("transfer_data")
    @Expose
    private
    String transfer_data;

    public List<PayfriendDataModel> getData() {
        return data;
    }

    public String getBuss_scheme_code() {
        return buss_scheme_code;
    }

    public String getBuss_scheme_name() {
        return buss_scheme_name;
    }

    public String getTransfer_data() {
        return transfer_data;
    }
}
