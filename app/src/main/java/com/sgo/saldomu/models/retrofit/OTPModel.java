package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OTPModel extends jsonModel{
    @SerializedName("otp_member")
    @Expose
    private
    String otp_member;

    @SerializedName("is_new")
    @Expose
    private
    String is_new;

    @SerializedName("is_pos")
    @Expose
    private
    String is_pos;

    @SerializedName("additional_fee")
    @Expose
    private
    String additional_fee;

    @SerializedName("ref_id")
    @Expose
    private
    String ref_id;

    public String getOtp_member() {
        return otp_member;
    }

    public String getIs_new() {
        return is_new;
    }

    public String getIs_pos() {
        return is_pos;
    }

    public String getAdditional_fee() {
        return additional_fee;
    }

    public String getRef_id() {
        return ref_id;
    }
}
