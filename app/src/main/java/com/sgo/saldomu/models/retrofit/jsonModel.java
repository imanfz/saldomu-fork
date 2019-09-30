package com.sgo.saldomu.models.retrofit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class jsonModel {
    @SerializedName("error_code")
    private String error_code;
    @SerializedName("error_message")
    private String error_message;
    @SerializedName("on_error")
    private boolean on_error;
//    @SerializedName("app_data")
//    @Expose
//    private List<AppDataModel> app_data = null;

    public String getError_code() {
        if (error_code == null)
            error_code = "1111";
        return error_code;
    }

    public String getError_message() {
        if (error_message == null)
            error_message = "";
        return error_message;
    }

    public boolean getOn_error() {
        return on_error;
    }

//    public List<AppDataModel> getApp_data() {
//        return app_data;
//    }
}
