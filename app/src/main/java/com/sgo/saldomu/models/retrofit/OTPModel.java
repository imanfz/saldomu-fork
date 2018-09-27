package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OTPModel extends jsonModel{
    @SerializedName("otp_member")
    @Expose
    private
    String otp_member;

    public String getOtp_member() {
        return otp_member;
    }
}
