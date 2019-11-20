package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetHelpModel extends jsonModel {
    @SerializedName("contact_data")
    @Expose
    private
    List<ContactDataModel> contact_data;

    public List<ContactDataModel> getContact_data() {
        return contact_data;
    }
}
