package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CommunityModel {
    @SerializedName("comm_id")
    @Expose
    private
    String comm_id;
    @SerializedName("comm_code")
    @Expose
    private
    String comm_code;
    @SerializedName("comm_name")
    @Expose
    private
    String comm_name;
    @SerializedName("api_key")
    @Expose
    private
    String api_key;
    @SerializedName("member_code")
    @Expose
    private
    String member_code;
    @SerializedName("callback_url")
    @Expose
    private
    String callback_url;

    public String getComm_id() {
        return comm_id;
    }

    public String getComm_code() {
        return comm_code;
    }

    public String getComm_name() {
        return comm_name;
    }

    public String getApi_key() {
        return api_key;
    }

    public String getMember_code() {
        return member_code;
    }

    public String getCallback_url() {
        return callback_url;
    }
}
