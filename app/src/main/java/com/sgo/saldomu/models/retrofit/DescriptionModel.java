package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DescriptionModel {
    @SerializedName("No. Handphone Tujuan")
    @Expose
    private
    String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
