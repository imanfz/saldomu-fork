package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ContactDataModel {
    @SerializedName("description")
    @Expose
    private
    String description;
    @SerializedName("name")
    @Expose
    private
    String name;
    @SerializedName("contact_phone")
    @Expose
    private
    String contact_phone;
    @SerializedName("contact_email")
    @Expose
    private
    String contact_email;

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getContact_phone() {
        return contact_phone;
    }

    public String getContact_email() {
        return contact_email;
    }
}
