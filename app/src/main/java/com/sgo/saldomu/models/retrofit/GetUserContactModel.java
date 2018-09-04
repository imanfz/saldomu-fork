package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetUserContactModel extends jsonModel {
    @SerializedName("contact_data")
    @Expose
    private
    String contact_data;

    public String getContact_data() {
        return contact_data;
    }
}
