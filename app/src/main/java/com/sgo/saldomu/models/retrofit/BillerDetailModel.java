package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BillerDetailModel {
    @SerializedName("No. Handphone Tujuan")
    @Expose
    private
    String phoneNumber;
    @SerializedName("No. Referensi")
    @Expose
    private
    String referenceCode;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getReferenceCode() {
        return referenceCode;
    }
}
