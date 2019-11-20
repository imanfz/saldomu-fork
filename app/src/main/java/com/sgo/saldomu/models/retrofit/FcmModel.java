package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FcmModel extends jsonModel {
    @SerializedName("uid")
    @Expose
    private
    String uid;

    public String getUid() {
        return uid;
    }
}
