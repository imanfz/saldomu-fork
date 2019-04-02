package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetAppVersionModel extends jsonModel {
    @SerializedName("ref")
    @Expose
    private
    String ref;
    @SerializedName("app_data")
    @Expose
    private
    AppDataModel app_data;

    public String getRef() {
        return ref;
    }

    public AppDataModel getApp_data() {
        return app_data;
    }
}
