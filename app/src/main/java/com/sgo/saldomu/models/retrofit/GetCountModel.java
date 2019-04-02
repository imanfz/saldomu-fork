package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetCountModel extends jsonModel {
    @SerializedName("count")
    @Expose
    private
    String count;

    public String getCount() {
        return count;
    }
}
