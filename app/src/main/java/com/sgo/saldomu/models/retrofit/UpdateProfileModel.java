package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateProfileModel extends jsonModel {
    @SerializedName("date_of_birth")
    @Expose
    private
    String date_of_birth;
    @SerializedName("email")
    @Expose
    private
    String email;
    @SerializedName("full_name")
    @Expose
    private
    String full_name;
    @SerializedName("verified")
    @Expose
    private
    String verified;
    @SerializedName("verified")
    @Expose
    private
    int verifieds;

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public String getEmail() {
        return email;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getVerified() {
        if (verified == null)
            verified = "";
        return verified;
    }

    public int getVerifieds() {
        return verifieds;
    }

    public void setVerifieds(int verifieds) {
        this.verifieds = verifieds;
    }
}
