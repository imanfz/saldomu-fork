package com.sgo.saldomu.models.retrofit;

import com.google.gson.JsonArray;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BBSRetrieveBankModel extends jsonModel{
    @SerializedName("bank_data")
    @Expose
    private
    JsonArray bank_data;
    @SerializedName("comm_id")
    @Expose
    private
    String comm_id;
    @SerializedName("comm_name")
    @Expose
    private
    String comm_name;
    @SerializedName("comm_code")
    @Expose
    private
    String comm_code;
    @SerializedName("api_key")
    @Expose
    private
    String api_key;
    @SerializedName("callback_url")
    @Expose
    private
    String callback_url;

    public JsonArray getBank_data() {
        return bank_data;
    }

    public String getComm_id() {
        return comm_id;
    }

    public String getComm_name() {
        return comm_name;
    }

    public String getComm_code() {
        return comm_code;
    }

    public String getApi_key() {
        return api_key;
    }

    public String getCallback_url() {
        return callback_url;
    }
}
