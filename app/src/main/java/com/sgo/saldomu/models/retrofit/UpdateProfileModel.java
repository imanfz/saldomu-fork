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
    @SerializedName("address")
    @Expose
    private
    String address;
    @SerializedName("bio")
    @Expose
    private
    String bio;
    @SerializedName("country")
    @Expose
    private
    String country;
    @SerializedName("social_id")
    @Expose
    private
    String social_id;
    @SerializedName("hobby")
    @Expose
    private
    String hobby;
    @SerializedName("birth_place")
    @Expose
    private
    String birth_place;
    @SerializedName("gender")
    @Expose
    private
    String gender;
    @SerializedName("idtype")
    @Expose
    private
    String idtype;
    @SerializedName("mother_name")
    @Expose
    private
    String mother_name;

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

    public String getAddress() {
        return address;
    }

    public String getBio() {
        return bio;
    }

    public String getCountry() {
        return country;
    }

    public String getSocial_id() {
        return social_id;
    }

    public String getHobby() {
        return hobby;
    }

    public String getBirth_place() {
        return birth_place;
    }

    public String getGender() {
        return gender;
    }

    public String getIdtype() {
        return idtype;
    }

    public String getMother_name() {
        return mother_name;
    }
}
